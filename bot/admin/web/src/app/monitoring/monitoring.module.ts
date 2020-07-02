/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {InfiniteScrollModule} from "ngx-infinite-scroll";
import {SharedModule} from "../shared-nlp/shared.module";
import {AuthGuard} from "../core-nlp/auth/auth.guard";
import {MonitoringTabsComponent} from "./monitoring-tabs.component";
import {ApplicationResolver} from "../core-nlp/application.resolver";
import {HistoryComponent} from "./history/history.component";
import {MonitoringService} from "./monitoring.service";
import {BotSharedModule} from "../shared/bot-shared.module";
import {MomentModule} from "ngx-moment";
import {DialogsComponent} from "./dialogs/dialogs.component";
import {MatNativeDateModule} from "@angular/material/core";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {
  NbAccordionModule,
  NbButtonModule,
  NbCalendarModule,
  NbCardModule,
  NbCheckboxModule,
  NbContextMenuModule,
  NbDatepickerModule,
  NbInputModule,
  NbListModule,
  NbMenuModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTooltipModule,
  NbUserModule,
  NbCalendarRangeModule
} from "@nebular/theme";
import {AnalyticsComponent} from './analytics/analytics.component';
import {GoogleChartsModule} from 'angular-google-charts';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: MonitoringTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        component: HistoryComponent
      },
      {
        path: 'dialogs',
        component: DialogsComponent
      },
      {
        path: 'history',
        component: HistoryComponent
      },
      {
        path: 'analytics',
        component: AnalyticsComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MonitoringRoutingModule {
}

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    MonitoringRoutingModule,
    InfiniteScrollModule,
    MomentModule,
    BotSharedModule,
    MatDatepickerModule,
    MatNativeDateModule,
    NbRouteTabsetModule,
    NbCheckboxModule,
    NbCardModule,
    NbTooltipModule,
    NbSpinnerModule,
    NbButtonModule,
    NbInputModule,
    NbSelectModule,
    NbCalendarModule,
    NbUserModule,
    NbDatepickerModule,
    NbListModule,
    NbAccordionModule,
    GoogleChartsModule,
    NbContextMenuModule,
    NbMenuModule.forRoot(),
    NbCalendarRangeModule
  ],
  declarations: [
    MonitoringTabsComponent,
    DialogsComponent,
    HistoryComponent,
    AnalyticsComponent
  ],
  exports: [],
  providers: [
    MonitoringService
  ],
  entryComponents: []
})
export class BotMonitoringModule {
}
