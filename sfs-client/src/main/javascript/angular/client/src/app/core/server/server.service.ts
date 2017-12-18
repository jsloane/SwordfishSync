import { Injectable } from '@angular/core';
import { Http, Response, RequestOptions, Headers, Request, RequestMethod, URLSearchParams } from '@angular/http';
import { HttpClient, HttpRequest, HttpParams, HttpResponse, HttpErrorResponse } from '@angular/common/http';
// import { Observable } from 'rxjs/Rx';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
// import 'rxjs/Rx';
import { MatDialog, MatDialogRef, MatDialogConfig } from '@angular/material';
import { ServerErrorComponent } from './server-error.component';

@Injectable()
export class ServerService {

    public headers: Headers;
    public requestOptions: RequestOptions;
    public res: Response;

    constructor(
        public httpClient: HttpClient,
        public http: Http,
        public dialog: MatDialog
    ) { }

    // TODO accept class to expect in response

    public postRequest(url: string, data: any, httpParams: HttpParams): any {

        /*this.headers = new Headers();
        this.headers.append('Content-type', 'application/json');

        this.requestOptions = new RequestOptions({
            method: RequestMethod.Post,
            url: url,
            headers: this.headers,
            body: JSON.stringify(data),
            params: urlSearchParams
        });

        return this.http.request(new Request(this.requestOptions))
            .map((res: Response) => {
                return res.json();
            })
            .catch(err => this.handleError(err));*/

        const requestOptions = {
            params: httpParams
        };

        return this.httpClient.post(url, data, requestOptions)
            .map((response: HttpResponse<any>) => {
                return response;
            })
            .catch((error: HttpErrorResponse) => this.handleError(error));

    }

    public getRequest(url: string, httpParams: HttpParams): any {

        /*this.headers = new Headers({
            'Cache-Control': 'no-cache',
            'Pragma': 'no-cache',
            'Expires': 'Sat, 01 Jan 2000 00:00:00 GMT'
        });
        this.headers.append('Content-type', 'application/json');

        this.requestOptions = new RequestOptions({
            method: RequestMethod.Get,
            url: url,
            headers: this.headers,
            params: urlSearchParams
        });

        return this.http.request(new Request(this.requestOptions))
            .map((res: Response) => {
                return res.json();
            })
            .catch(err => this.handleError(err));*/

        const requestOptions = {
            params: httpParams
        };

        return this.httpClient.get(url, requestOptions)
            .map((response: HttpResponse<any>) => {
                return response;
            })
            .catch((error: HttpErrorResponse) => this.handleError(error));

    }

    public putRequest(url: string, data: any, httpParams: HttpParams): any {

        /*this.headers = new Headers();
        this.headers.append('Content-type', 'application/json');

        this.requestOptions = new RequestOptions({
            method: RequestMethod.Put,
            url: url,
            headers: this.headers,
            body: JSON.stringify(data),
            params: urlSearchParams
        });

        return this.http.request(new Request(this.requestOptions))
            .map((res: Response) => {
                return res.json();
            })
            .catch(err => this.handleError(err));*/

        const requestOptions = {
            params: httpParams
        };

        return this.httpClient.put(url, data, requestOptions)
            .map((response: HttpResponse<any>) => {
                return response;
            })
            .catch((error: HttpErrorResponse) => this.handleError(error));
    }

    public deleteRequest(url: string, httpParams: HttpParams): any {

        /*this.headers = new Headers();

        this.requestOptions = new RequestOptions({
            method: RequestMethod.Delete,
            url: url,
            headers: this.headers,
            params: urlSearchParams
        });

        return this.http.request(new Request(this.requestOptions))
            .map((res: Response) => {
                return res.json();
            })
            .catch(err => this.handleError(err));*/

        const requestOptions = {
            params: httpParams
        };

        return this.httpClient.delete(url, requestOptions)
            .map((response: HttpResponse<any>) => {
                return response;
            })
            .catch((error: HttpErrorResponse) => this.handleError(error));

    }

    private handleError(httpError: HttpErrorResponse) {
        // HTTP error. Display popup including error message and reference
        let dialogRef: MatDialogRef<ServerErrorComponent>;
        dialogRef = this.dialog.open(ServerErrorComponent, {
            data: {
                httpError: httpError
            },
            disableClose: true
            // TODO display validation errors
        });

        console.error(httpError);
        return Observable.throw(httpError);
    }

}
