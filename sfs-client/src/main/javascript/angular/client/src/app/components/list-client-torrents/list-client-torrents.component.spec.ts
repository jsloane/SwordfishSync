import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ListClientTorrentsComponent } from './list-client-torrents.component';

describe('ListClientTorrentsComponent', () => {
  let component: ListClientTorrentsComponent;
  let fixture: ComponentFixture<ListClientTorrentsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ListClientTorrentsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListClientTorrentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
