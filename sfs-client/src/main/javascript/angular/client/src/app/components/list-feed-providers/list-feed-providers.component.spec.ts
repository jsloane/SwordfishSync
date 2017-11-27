import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ListFeedProvidersComponent } from './list-feed-providers.component';

describe('ListFeedProvidersComponent', () => {
  let component: ListFeedProvidersComponent;
  let fixture: ComponentFixture<ListFeedProvidersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ListFeedProvidersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListFeedProvidersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
