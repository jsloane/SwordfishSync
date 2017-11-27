import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageFilterAttributesComponent } from './manage-filter-attributes.component';

describe('ManageFilterAttributesComponent', () => {
  let component: ManageFilterAttributesComponent;
  let fixture: ComponentFixture<ManageFilterAttributesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManageFilterAttributesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageFilterAttributesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
