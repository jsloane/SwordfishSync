import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLinkActive } from '@angular/router';
/*import { SubjectOutlineService } from '../core/service/subject-outline.service';
import { UserOutlinesService } from '../core/service/user-outlines.service';
import { UserService } from '../core/service/user.service';
import { Outline } from '../core/model/outline';
import { OutlineListComponent } from './outline-list/outline-list.component';
import { UserContext } from '../core/model/user-context';*/

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

    authorCount = 0;
    qaCount = 0;

    constructor(
        // public userOutlinesService: UserOutlinesService
    ) { }

    ngOnInit() {
        // Load outlines to determine the number of outlines
        /*this.userOutlinesService.loadOutlines();
        this.userOutlinesService.authorOutlineArray.subscribe(authorOutlineArray => {
            this.authorCount = authorOutlineArray.length;
        });
        this.userOutlinesService.qaOutlineArray.subscribe(qaOutlineArray => {
            this.qaCount = qaOutlineArray.length;
        });*/
    }

}
