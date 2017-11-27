import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditFeedProviderComponent } from './edit-feed-provider.component';

describe('EditFeedProviderComponent', () => {
  let component: EditFeedProviderComponent;
  let fixture: ComponentFixture<EditFeedProviderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditFeedProviderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditFeedProviderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
