import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ListTorrentsComponent } from './list-torrents.component';

describe('ListTorrentsComponent', () => {
  let component: ListTorrentsComponent;
  let fixture: ComponentFixture<ListTorrentsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ListTorrentsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListTorrentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
