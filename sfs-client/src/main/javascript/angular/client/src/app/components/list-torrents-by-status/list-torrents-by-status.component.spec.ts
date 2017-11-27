import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ListTorrentsByStatusComponent } from './list-torrents-by-status.component';

describe('ListTorrentsByStatusComponent', () => {
  let component: ListTorrentsByStatusComponent;
  let fixture: ComponentFixture<ListTorrentsByStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ListTorrentsByStatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListTorrentsByStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
