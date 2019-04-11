package swordfishsync.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import swordfishsync.domain.TorrentState;
import swordfishsync.service.MessageService;
import swordfishsync.service.SettingService;
import swordfishsync.service.TorrentStateService;
import swordfishsync.service.dto.ConfigurationDto;
import swordfishsync.service.dto.MessageDto;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Resource
	SettingService settingService;

	@Resource
	MessageService messageService;

	@Resource
	TorrentStateService torrentStateService;

    @GetMapping("/configuration")
    @ResponseBody
    public ResponseEntity<ConfigurationDto> getConfiguration() {
    	ConfigurationDto config = settingService.getConfiguration();
        return new ResponseEntity<ConfigurationDto>(config, HttpStatus.OK);
    }

    @RequestMapping(value = "/configuration/settings", method = RequestMethod.PUT)
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settingCodeValues) {
    	settingService.setSettings(settingCodeValues);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/messages")
    @ResponseBody
    public ResponseEntity<List<MessageDto>> getMessages() {
        return new ResponseEntity<List<MessageDto>>(messageService.getMessages(), HttpStatus.OK);
    }

    @RequestMapping(value = "/messages/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable Long id) {
    	messageService.deleteMessage(id);
    }

    @RequestMapping(value = "/purgeInprogressTorrents", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> purgeInprogressTorrents() {
		return new ResponseEntity<List<String>>(
			torrentStateService.purgeTorrentStates(Collections.singletonList(TorrentState.Status.IN_PROGRESS)),
			HttpStatus.OK
		);
    }

}
