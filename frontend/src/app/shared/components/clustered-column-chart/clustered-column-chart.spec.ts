import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClusteredColumnChart } from './clustered-column-chart';

describe('ClusteredColumnChart', () => {
  let component: ClusteredColumnChart;
  let fixture: ComponentFixture<ClusteredColumnChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClusteredColumnChart],
    }).compileComponents();

    fixture = TestBed.createComponent(ClusteredColumnChart);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
