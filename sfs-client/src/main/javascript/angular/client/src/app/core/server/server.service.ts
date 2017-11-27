import { Injectable } from '@angular/core';
import { Http, Response, RequestOptions, Headers, Request, RequestMethod, URLSearchParams } from '@angular/http';
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
        public http: Http,
        public dialog: MatDialog
    ) { }

    public postRequest(url: string, data: any, urlSearchParams: URLSearchParams): any {

        this.headers = new Headers();
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
            .catch(err => this.handleError(err));

    }

    public getRequest(url: string, urlSearchParams: URLSearchParams): any {

        this.headers = new Headers({
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
            .catch(err => this.handleError(err));

    }

    public putRequest(url: string, data: any, urlSearchParams: URLSearchParams): any {

        this.headers = new Headers();
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
            .catch(err => this.handleError(err));

    }

    public deleteRequest(url: string, urlSearchParams: URLSearchParams): any {

        this.headers = new Headers();

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
            .catch(err => this.handleError(err));

    }

    private handleError(error: Response | any) {

        let errorMessage: string;
        let userMessage: string;
        let userErrorMessage: string;
        let errorReference: string;

        if (error instanceof Response) {
            errorMessage = error.status + ' - ' + (error.statusText || '');

            let errorJson = null;
            try {
                errorJson = error.json();
            } catch (e) { }

            if (errorJson) {
                errorMessage += ' ' + JSON.stringify(errorJson);
            }

            if (errorJson && errorJson.userMessage) {
                userMessage = errorJson.userMessage;
            } else {
                userMessage = error.statusText;
            }

            if (errorJson && errorJson.errorReference) {
                errorReference = errorJson.errorReference;
                userErrorMessage = userMessage + '. Reference: ' + errorReference;
            }

            // if (error.status === 500 || error.status === 400 || error.status === 404) {
                // Internal server error, or server not found. Display popup including error message and reference
                let dialogRef: MatDialogRef<ServerErrorComponent>;
                dialogRef = this.dialog.open(ServerErrorComponent, {
                    data: {
                        'userMessage': userMessage,
                        'errorReference': errorReference
                    }
                    // TODO keep open if clicked outside
                });
            // }
        } else {
            errorMessage = error.message ? error.message : error.toString();
        }

        console.error(errorMessage);
        if (userErrorMessage) {
            return Observable.throw('Error: ' + userErrorMessage);
        } else {
            return Observable.throw('Error: ' + errorMessage);
        }

    }

}
