package swordfishsync.torrentclient.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.util.CollectionUtils;

import ca.benow.transmission.AddTorrentParameters;
import ca.benow.transmission.SetTorrentParameters;
import ca.benow.transmission.TransmissionClient;
import ca.benow.transmission.model.AddedTorrentInfo;
import ca.benow.transmission.model.TorrentStatus;
import swordfishsync.domain.Torrent;
import swordfishsync.domain.TorrentState;
import swordfishsync.exceptions.TorrentClientException;
import swordfishsync.model.TorrentDetails;
import swordfishsync.torrentclient.TorrentClient;

public class TransmissionTorrentClient implements TorrentClient {

	TransmissionClient transmissionClient;
	Cache<String, List> torrentClientCache;

	public TransmissionTorrentClient(TransmissionClient transmissionClient) {
		this.transmissionClient = transmissionClient;

		// store torrent data in short term cache
		String cacheName = "sfs-server-transmissionClientData";

		// note error when accessing UI Home for first time, unless sync service already accessed transmission
		
		/*
		 * 
		 * 
		 * 
		 * exception

org.springframework.web.util.NestedServletException: Request processing failed; nested exception is javax.cache.CacheException: A Cache named [sfs-server-transmissionClientData] already exists
	org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:982)
	org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:861)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:624)
	org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:846)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:731)
	org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)
	swordfishsync.security.SecurityDelegatingFilterProxy.doFilter(SecurityDelegatingFilterProxy.java:22)
root cause

javax.cache.CacheException: A Cache named [sfs-server-transmissionClientData] already exists
	org.ehcache.jsr107.Eh107CacheManager.createCache(Eh107CacheManager.java:202)
	swordfishsync.torrentclient.impl.TransmissionTorrentClient.<init>(TransmissionTorrentClient.java:57)
	swordfishsync.service.TorrentClientService.setTorrentClient(TorrentClientService.java:64)
	swordfishsync.service.TorrentClientService.getTorrentClient(TorrentClientService.java:42)
	swordfishsync.service.TorrentClientService.getTorrentDetails(TorrentClientService.java:121)
	swordfishsync.service.impl.TorrentStateServiceImpl$1.convert(TorrentStateServiceImpl.java:46)
	swordfishsync.service.impl.TorrentStateServiceImpl$1.convert(TorrentStateServiceImpl.java:41)
	org.springframework.data.domain.Chunk.getConvertedContent(Chunk.java:168)
	org.springframework.data.domain.PageImpl.map(PageImpl.java:104)
	swordfishsync.service.impl.TorrentStateServiceImpl.getTorrentStatesByStatuses(TorrentStateServiceImpl.java:41)
	sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	java.lang.reflect.Method.invoke(Method.java:498)
	org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:333)
	org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190)
	org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157)
	org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:99)
	org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:282)
	org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:96)
	org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
	org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:213)
	com.sun.proxy.$Proxy85.getTorrentStatesByStatuses(Unknown Source)
	swordfishsync.controllers.TorrentController.getTorrentsByStatuses(TorrentController.java:66)
	sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	java.lang.reflect.Method.invoke(Method.java:498)
	org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)
	org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:133)
	org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:97)
	org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:827)
	org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:738)
	org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85)
	org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:967)
	org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:901)
	org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970)
	org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:861)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:624)
	org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:846)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:731)
	org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)
	swordfishsync.security.SecurityDelegatingFilterProxy.doFilter(SecurityDelegatingFilterProxy.java:22)
root cause

org.ehcache.jsr107.MultiCacheException: [Exception 0] Cache 'sfs-server-transmissionClientData' already exists
	org.ehcache.jsr107.Eh107CacheManager.createCache(Eh107CacheManager.java:200)
	swordfishsync.torrentclient.impl.TransmissionTorrentClient.<init>(TransmissionTorrentClient.java:57)
	swordfishsync.service.TorrentClientService.setTorrentClient(TorrentClientService.java:64)
	swordfishsync.service.TorrentClientService.getTorrentClient(TorrentClientService.java:42)
	swordfishsync.service.TorrentClientService.getTorrentDetails(TorrentClientService.java:121)
	swordfishsync.service.impl.TorrentStateServiceImpl$1.convert(TorrentStateServiceImpl.java:46)
	swordfishsync.service.impl.TorrentStateServiceImpl$1.convert(TorrentStateServiceImpl.java:41)
	org.springframework.data.domain.Chunk.getConvertedContent(Chunk.java:168)
	org.springframework.data.domain.PageImpl.map(PageImpl.java:104)
	swordfishsync.service.impl.TorrentStateServiceImpl.getTorrentStatesByStatuses(TorrentStateServiceImpl.java:41)
	sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	java.lang.reflect.Method.invoke(Method.java:498)
	org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:333)
	org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190)
	org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157)
	org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:99)
	org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:282)
	org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:96)
	org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
	org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:213)
	com.sun.proxy.$Proxy85.getTorrentStatesByStatuses(Unknown Source)
	swordfishsync.controllers.TorrentController.getTorrentsByStatuses(TorrentController.java:66)
	sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	java.lang.reflect.Method.invoke(Method.java:498)
	org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)
	org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:133)
	org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:97)
	org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:827)
	org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:738)
	org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85)
	org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:967)
	org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:901)
	org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970)
	org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:861)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:624)
	org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:846)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:731)
	org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)
	swordfishsync.security.SecurityDelegatingFilterProxy.doFilter(SecurityDelegatingFilterProxy.java:22)
note The full stack trace of the root cause is available in the Apache Tomcat/7.0.68 (Ubuntu) logs.


		 */
		
		
		
		// store torrent details in cache
		CachingProvider provider = Caching.getCachingProvider();
	    CacheManager cacheManager = provider.getCacheManager();
	    
	    torrentClientCache = cacheManager.getCache(cacheName, String.class, List.class);
	    
	    if (torrentClientCache == null) {
		    MutableConfiguration<String, List> configuration = // TODO List<TorrentStatus>
		            new MutableConfiguration<String, List>()
		                .setTypes(String.class, List.class)
		                .setStoreByValue(false)
		                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
			torrentClientCache = cacheManager.createCache(cacheName, configuration);
	    }
	}
	
	@Override
	public void addTorrent(TorrentState torrentState) throws TorrentClientException {
		AddTorrentParameters newTorrentParameters = new AddTorrentParameters(torrentState.getTorrent().getUrl());
		try {
			AddedTorrentInfo ati = transmissionClient.addTorrent(newTorrentParameters);
			
			// set torrent details
			torrentState.getTorrent().setHashString(ati.getHashString());
			torrentState.getTorrent().setClientTorrentId(ati.getId());
			if (torrentState.getTorrent().getName() == null) {
				torrentState.getTorrent().setName(ati.getName());
			}
			
			if (torrentState.getFeedProvider().getUploadLimit() > 0) {
				// set upload limit
				SetTorrentParameters setTorrentParameters = new SetTorrentParameters(ati.getId());
				setTorrentParameters.setUploadLimit(torrentState.getFeedProvider().getUploadLimit());
				transmissionClient.setTorrents(setTorrentParameters);
			}
		} catch (IOException e) {
			throw new TorrentClientException("An error occurring adding the torrent to Transmission: " + e.getMessage(), e);
		}
	}

	@Override
	public void moveTorrent(Torrent torrent, String directory) throws TorrentClientException {
		try {
			transmissionClient.moveTorrents(new Object[] {torrent.getHashString()}, directory, true);
		} catch (IOException e) {
			throw new TorrentClientException(String.format("Error moving torrent [%s]", torrent.getName()), e);
		}
	}

	@Override
	public void removeTorrent(Torrent torrent, Boolean deleteData) throws TorrentClientException {
		try {
			transmissionClient.removeTorrents(new Object[] {torrent.getHashString()}, deleteData);
		} catch (IOException e) {
			throw new TorrentClientException(String.format("Error removing torrent [%s]", torrent.getName()), e);
		}
	}

	@Override
	public TorrentDetails getTorrentDetails(Torrent torrent, Boolean includeFiles) throws TorrentClientException {
		TorrentDetails torrentDetails = new TorrentDetails();
		torrentDetails.setStatus(TorrentDetails.Status.UNKNOWN);
		List<TorrentStatus> torrentStatuses = new ArrayList<TorrentStatus>();
		
		String cacheKey = "allTorrents";
	    
		if (torrentClientCache != null) {
			// get cached data if available
			torrentStatuses = torrentClientCache.get(cacheKey);
		}
		
		if (CollectionUtils.isEmpty(torrentStatuses)) {
			try {
				torrentStatuses = transmissionClient.getAllTorrents(
					new TorrentStatus.TorrentField[] {
						TorrentStatus.TorrentField.id,
						TorrentStatus.TorrentField.activityDate,
						TorrentStatus.TorrentField.status,
						TorrentStatus.TorrentField.hashString,
						TorrentStatus.TorrentField.files,
						TorrentStatus.TorrentField.percentDone,
						TorrentStatus.TorrentField.downloadDir
					}
				);
				
				if (torrentClientCache != null && torrentStatuses != null) {
					torrentClientCache.put(cacheKey, torrentStatuses);
				}
			} catch (IOException e) {
				throw new TorrentClientException("Error getting torrent details for torrent [" + torrent.getName() + "]", e);
				//log.error('Error fetching and caching torrent status', e)
				//e.printStackTrace()
			}
		}
		
		// hashstring being deleted by quartz job
		if (torrent != null) {
			for (TorrentStatus torrentStatus : torrentStatuses) {
				//println 'torrentStatus.getField(TorrentStatus.TorrentField.hashString): ' + torrentStatus.getField(TorrentStatus.TorrentField.hashString)
				if (torrentStatus != null && StringUtils.isNotBlank(torrent.getHashString()) && torrent.getHashString().equals(torrentStatus.getField(TorrentStatus.TorrentField.hashString))) {
					// set status
					//println 'FOUND TORRENT STATUS: ' + torrentStatus
					torrentDetails = setTorrentDetails(includeFiles, torrentStatus);
					break;
				}
			}
		}
		
		return torrentDetails;
	}

	private TorrentDetails setTorrentDetails(Boolean includeFiles, TorrentStatus torrentStatus) {
		TorrentDetails torrentDetails = new TorrentDetails();
		
		// set status
		switch(torrentStatus.getStatus()) {
			case downloadWait:
				torrentDetails.setStatus(TorrentDetails.Status.QUEUED);
				break;
			case downloading:
				torrentDetails.setStatus(TorrentDetails.Status.DOWNLOADING);
				break;
			case seedWait:
				torrentDetails.setStatus(TorrentDetails.Status.SEEDWAIT);
				break;
			case seeding:
				torrentDetails.setStatus(TorrentDetails.Status.SEEDING);
				break;
			case finished:
				torrentDetails.setStatus(TorrentDetails.Status.FINISHED);
				break;
			default:
				torrentDetails.setStatus(TorrentDetails.Status.UNKNOWN);
				break;
		}
		
		// set downloaded directory
		torrentDetails.setDownloadedToDirectory(torrentStatus.getField(TorrentStatus.TorrentField.downloadDir).toString());
		
		// set percent done
		torrentDetails.setPercentDone(torrentStatus.getPercentDone());
		
		// set files
		if (includeFiles) {
			JSONTokener tokener = new JSONTokener(torrentStatus.getField(TorrentStatus.TorrentField.files).toString());
			JSONArray fileArray = new JSONArray(tokener);
			
			for (int i = 0; i < fileArray.length(); i++) {
				JSONObject obj = new JSONObject(fileArray.get(i).toString());
				String filename = obj.getString("name");
				torrentDetails.getFiles().add(filename);
			}
		}
		
		return torrentDetails;
	}

	public List<TorrentDetails> getAllTorrents() throws TorrentClientException {
		List<TorrentDetails> allTorrents = new ArrayList<TorrentDetails>();
		try {
			List<TorrentStatus> allTorrentStatuses = transmissionClient.getAllTorrents(
					new TorrentStatus.TorrentField[] {
							TorrentStatus.TorrentField.all
					}
			);
			for (TorrentStatus torrentStatus : allTorrentStatuses) {
				TorrentDetails torrentDetails = setTorrentDetails(false, torrentStatus);
				torrentDetails.setName(torrentStatus.getField(TorrentStatus.TorrentField.name).toString());
				Integer activityDate = (Integer) torrentStatus.getField(TorrentStatus.TorrentField.activityDate);
				if (activityDate != null && activityDate > 0) {
					torrentDetails.setActivityDate(new Date(activityDate * 1000L));
				}
				allTorrents.add(torrentDetails);
			}
		} catch (IOException e) {
			throw new TorrentClientException("Error getting all torrents", e);
		}
		return allTorrents;
	}
	
}
