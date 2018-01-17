import Dashboard from "./components/Dashboard.vue"
import Login from "./components/Login.vue"
import SystemManagement from './components/SystemManagement.vue'
import AddUser from './components/AddUser.vue'
import DelUser from './components/DelUser.vue'
import UpdateUser from './components/UpdateUser.vue'
import VehicleOps from './components/VehicleOps.vue'

import Order from './components/Order.vue'
import NewOrder from './components/NewOrder.vue'
import UnhandledOrder from './components/UnhandledOrder.vue'
import MyOrder from "./components/MyOrder.vue"
import QueryOrder from "./components/QueryOrder.vue"

import CareHouse from './components/CareHouse.vue'
import QueryCareHouse from './components/QueryCareHouse.vue'

import BuildCase from './components/BuildCase.vue'
import QueryBuildCase from './components/QueryBuildCase.vue'
import ImportBuildCase from './components/ImportBuildCase.vue'
import NewBuildCase from './components/NewBuildCase.vue'

import OilUser from './components/OilUser.vue'
import QueryOilUser from './components/QueryOilUser.vue'

import Intern from './components/Intern.vue'
import UpdateBuilder from './components/UpdateBuilder.vue'
import UpdateBuildCase from './components/UpdateBuildCase.vue'
import UsageReport from './components/UsageReport.vue'

export const routes = [{
        path: '/',
        component: Dashboard,
        name: 'Dashboard'
    },
    {
        path: '/Login',
        component: Login,
        name: 'Login'
    },
    {
        path: '/Vehicle',
        component: VehicleOps,
        name: 'VehicleOps',
        children: [{
                path: 'AddVehicle',
                component: AddUser,
                name: 'AddVehicle'
            },
            {
                path: 'DelVehicle',
                component: DelUser,
                name: 'DelVehicle'
            },
            {
                path: 'UpdateVehicle',
                component: UpdateUser,
                name: 'UpdateVehicle'
            },
        ]
    },
    {
        path: '/Order',
        component: Order,
        name: 'Order',
        children: [{
                path: 'Unhandled',
                component: UnhandledOrder,
                name: 'UnhandledOrder'
            },
            {
                path: 'New',
                component: NewOrder,
                name: 'NewOrder'
            },
            {
                path: 'Mine',
                component: MyOrder,
                name: 'MyOrder'
            },
            {
                path: 'Query',
                component: QueryOrder,
                name: 'QueryOrder'
            }
        ]
    },
    {
        path: '/CareHouse',
        component: CareHouse,
        name: 'CareHouse',
        children: [{
            path: 'Query',
            component: QueryCareHouse,
            name: 'QueryCareHouse'
        }]
    },
    {
        path: '/OilUser',
        component: OilUser,
        name: 'OilUser',
        children: [{
            path: 'Query',
            component: QueryOilUser,
            name: 'QueryOilUser'
        }]
    },
    {
        path: '/Intern',
        component: Intern,
        name: 'Intern',
        children: [{
                path: 'Builder',
                component: UpdateBuilder,
                name: 'UpdateBuilder'
            },
            {
                path: 'BuildCase',
                component: UpdateBuildCase,
                name: 'UpdateBuildCase'
            },
            {
                path: 'Report',
                component: UsageReport,
                name: 'Report'
            },
        ]
    },
    {
        path: '/BuildCase',
        component: BuildCase,
        name: 'BuildCase',
        children: [{
                path: 'New',
                component: NewBuildCase,
                name: 'NewBuildCase'
            },
            {
                path: 'Query',
                component: QueryBuildCase,
                name: 'QueryBuildCase'
            },
            {
                path: 'Import',
                component: ImportBuildCase,
                name: 'ImportBuildCase'
            }
        ]
    },
    {
        path: '/System',
        component: SystemManagement,
        name: 'SystemManagement',
        children: [{
                path: 'AddUser',
                component: AddUser,
                name: 'AddUser'
            },
            {
                path: 'DelUser',
                component: DelUser,
                name: 'DelUser'
            },
            {
                path: 'UpdateUser',
                component: UpdateUser,
                name: 'UpdateUser'
            },
        ]
    },
    {
        path: '*',
        redirect: '/'
    }
];