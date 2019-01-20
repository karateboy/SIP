import Main from '@/views/Main.vue';

// 不作为Main组件的子页面展示的页面单独写，如下
export const loginRouter = {
    path: '/login',
    name: 'login',
    meta: {
        title: '登入'
    },
    component: () =>
        import ('@/views/login.vue')
};

export const page404 = {
    path: '/*',
    name: 'error-404',
    meta: {
        title: '404-頁面不存在'
    },
    component: () =>
        import ('@/views/error-page/404.vue')
};

export const page403 = {
    path: '/403',
    meta: {
        title: '403-權限不足'
    },
    name: 'error-403',
    component: () =>
        import ('@//views/error-page/403.vue')
};

export const page500 = {
    path: '/500',
    meta: {
        title: '500-系統錯誤'
    },
    name: 'error-500',
    component: () =>
        import ('@/views/error-page/500.vue')
};

export const preview = {
    path: '/preview',
    name: 'preview',
    component: () =>
        import ('@/views/form/article-publish/preview.vue')
};

export const locking = {
    path: '/locking',
    name: 'locking',
    component: () =>
        import ('@/views/main-components/lockscreen/components/locking-page.vue')
};

// 作为Main组件的子页面展示但是不在左侧菜单显示的路由写在otherRouter里
export const otherRouter = {
    path: '/',
    name: 'otherRouter',
    redirect: '/home',
    component: Main,
    children: [{
            path: 'home',
            title: {
                i18n: 'home'
            },
            name: 'home_index',
            component: () =>
                import ('@/views/report/realtimeReport.vue')
        },
        {
            path: 'ownspace',
            title: '个人中心',
            name: 'ownspace_index',
            component: () =>
                import ('@/views/own-space/own-space.vue')
        },
        {
            path: 'order/:order_id',
            title: '订单详情',
            name: 'order-info',
            component: () =>
                import ('@/views/advanced-router/component/order-info.vue')
        }, // 用于展示动态路由
        {
            path: 'shopping',
            title: '购物详情',
            name: 'shopping',
            component: () =>
                import ('@/views/advanced-router/component/shopping-info.vue')
        }, // 用于展示带参路由
        {
            path: 'message',
            title: '消息中心',
            name: 'message_index',
            component: () =>
                import ('@/views/message/message.vue')
        }
    ]
};

// 作为Main组件的子页面展示并且在左侧菜单显示的路由写在appRouter里
export const appRouter = [{
        path: '/info',
        icon: 'information',
        name: 'information',
        title: '網站導覽',
        component: Main,
        children: [{
                path: 'subject',
                title: '網站主旨',
                name: 'subject',
                component: () =>
                    import ('@/views/static/subject.vue')
            },
            {
                path: 'siteMap',
                title: '網站地圖',
                name: 'siteMap',
                component: () =>
                    import ('@/views/static/siteMap.vue')
            },
            {
                path: 'privacy',
                title: '隱私權及資訊安全宣告',
                name: 'privacy',
                component: () =>
                    import ('@/views/static/privacy.vue')
            }
        ]
    },
    {
        path: '/realtime',
        icon: 'flash',
        name: 'realtime',
        title: '即時資訊',
        component: Main,
        children: [{
                path: 'trend',
                title: '即時趨勢',
                name: 'realtimeTrend',
                component: () =>
                    import ('@/views/historyTrend/realtimeTrend.vue')
            },
            {
                path: 'data',
                title: '即時數值',
                name: 'realtimeData',
                component: () =>
                    import ('@/views/report/realtimeReport.vue')
            }
        ]
    },
    {
        path: '/query',
        icon: 'search',
        name: 'query',
        title: '數據查詢',
        component: Main,
        children: [{
                path: 'data',
                title: '歷史資料查詢',
                name: 'historyData',
                component: () =>
                    import ('@/views/historyData/historyData.vue')
            },
            {
                path: 'trend',
                title: '歷史趨勢圖',
                name: 'historyTrend',
                component: () =>
                    import ('@/views/historyTrend/historyTrend.vue')
            },
            {
                path: 'alarm',
                title: '警報記錄查詢',
                name: 'alarm',
                component: () =>
                    import ('@/views/alarm/alarm.vue')
            },
            {
                path: 'rose',
                title: '玫瑰圖',
                name: 'winRose',
                component: () =>
                    import ('@/views/windRose/windRose.vue')
            }
        ]
    },
    {
        path: '/report',
        icon: 'document-text',
        name: 'report',
        title: '報表查詢',
        component: Main,
        children: [{
                path: 'monitor',
                title: '監測報表',
                name: 'monitorReport',
                component: () =>
                    import ('@/views/report/report.vue')
            },
            {
                path: 'monthlyHour',
                title: '月份時報表',
                name: 'monthlyHourReport',
                component: () =>
                    import ('@/views/report/monthlyHourReport.vue')
            },
            {
                path: 'audit',
                title: '資料檢核報表',
                name: 'auditReport',
                component: () =>
                    import ('@/views/historyData/auditReport.vue')
            }
        ]
    },
    {
        path: '/system',
        icon: 'settings',
        name: 'system',
        title: '系統管理',
        component: Main,
        children: [{
                path: 'monitorType',
                title: '測項管理',
                name: 'monitorTypeConfig',
                component: () =>
                    import ('@/views/config/monitorTypeConfig.vue')
            },
            {
                path: 'monitor',
                title: '測站管理',
                name: 'monitorConfig',
                component: () =>
                    import ('@/views/config/monitorConfig.vue')
            },
            {
                path: 'audit',
                title: '資料檢核管理',
                name: 'auditConfig',
                component: () =>
                    import ('@/views/config/auditConfig.vue')
            },

            {
                path: 'user',
                title: '使用者管理',
                name: 'userConfig',
                component: () =>
                    import ('@/views/static/todo.vue')
            }
        ]
    },
];

export const iappRouter = [{
        path: '/guide',
        icon: 'key',
        name: 'access',
        title: '權限管理',
        component: Main,
        children: [{
            path: 'index',
            title: '權限管理',
            name: 'access_index',
            component: () =>
                import ('@/views/access/access.vue')
        }]
    },
    {
        path: '/access-test',
        icon: 'lock-combination',
        title: '权限测试页',
        name: 'accesstest',
        access: 0,
        component: Main,
        children: [{
            path: 'index',
            title: '权限测试页',
            name: 'accesstest_index',
            access: 0,
            component: () =>
                import ('@/views/access/access-test.vue')
        }]
    },
    {
        path: '/international',
        icon: 'earth',
        title: {
            i18n: 'international'
        },
        name: 'international',
        component: Main,
        children: [{
            path: 'index',
            title: {
                i18n: 'international'
            },
            name: 'international_index',
            component: () =>
                import ('@/views/international/international.vue')
        }]
    },
    {
        path: '/component',
        icon: 'social-buffer',
        name: 'component',
        title: '组件',
        component: Main,
        children: [{
                path: 'text-editor',
                icon: 'compose',
                name: 'text-editor',
                title: '富文本编辑器',
                component: () =>
                    import ('@/views/my-components/text-editor/text-editor.vue')
            },
            {
                path: 'md-editor',
                icon: 'pound',
                name: 'md-editor',
                title: 'Markdown编辑器',
                component: () =>
                    import ('@/views/my-components/markdown-editor/markdown-editor.vue')
            },
            {
                path: 'image-editor',
                icon: 'crop',
                name: 'image-editor',
                title: '图片预览编辑',
                component: () =>
                    import ('@/views/my-components/image-editor/image-editor.vue')
            },
            {
                path: 'draggable-list',
                icon: 'arrow-move',
                name: 'draggable-list',
                title: '可拖拽列表',
                component: () =>
                    import ('@/views/my-components/draggable-list/draggable-list.vue')
            },
            {
                path: 'area-linkage',
                icon: 'ios-more',
                name: 'area-linkage',
                title: '城市级联',
                component: () =>
                    import ('@/views/my-components/area-linkage/area-linkage.vue')
            },
            {
                path: 'file-upload',
                icon: 'android-upload',
                name: 'file-upload',
                title: '文件上传',
                component: () =>
                    import ('@/views/my-components/file-upload/file-upload.vue')
            },
            {
                path: 'scroll-bar',
                icon: 'android-upload',
                name: 'scroll-bar',
                title: '滚动条',
                component: () =>
                    import ('@/views/my-components/scroll-bar/scroll-bar-page.vue')
            },
            {
                path: 'count-to',
                icon: 'arrow-graph-up-right',
                name: 'count-to',
                title: '数字渐变',
                // component: () => import('@/views/my-components/count-to/count-to.vue')
                component: () =>
                    import ('@/views/my-components/count-to/count-to.vue')
            },
            {
                path: 'split-pane-page',
                icon: 'ios-pause',
                name: 'split-pane-page',
                title: 'split-pane',
                component: () =>
                    import ('@/views/my-components/split-pane/split-pane-page.vue')
            }
        ]
    },
    {
        path: '/form',
        icon: 'android-checkbox',
        name: 'form',
        title: '表单编辑',
        component: Main,
        children: [{
                path: 'artical-publish',
                title: '文章发布',
                name: 'artical-publish',
                icon: 'compose',
                component: () =>
                    import ('@/views/form/article-publish/article-publish.vue')
            },
            {
                path: 'workflow',
                title: '工作流',
                name: 'workflow',
                icon: 'arrow-swap',
                component: () =>
                    import ('@/views/form/work-flow/work-flow.vue')
            }

        ]
    },
    // {
    //     path: '/charts',
    //     icon: 'ios-analytics',
    //     name: 'charts',
    //     title: '图表',
    //     component: Main,
    //     children: [
    //         { path: 'pie', title: '饼状图', name: 'pie', icon: 'ios-pie', component: resolve => { require('@/views/access/access.vue') },
    //         { path: 'histogram', title: '柱状图', name: 'histogram', icon: 'stats-bars', component: resolve => { require('@/views/access/access.vue') }

    //     ]
    // },
    {
        path: '/tables',
        icon: 'ios-grid-view',
        name: 'tables',
        title: '表格',
        component: Main,
        children: [{
                path: 'dragableTable',
                title: '可拖拽排序',
                name: 'dragable-table',
                icon: 'arrow-move',
                component: () =>
                    import ('@/views/tables/dragable-table.vue')
            },
            {
                path: 'editableTable',
                title: '可编辑表格',
                name: 'editable-table',
                icon: 'edit',
                component: () =>
                    import ('@/views/tables/editable-table.vue')
            },
            {
                path: 'searchableTable',
                title: '可搜索表格',
                name: 'searchable-table',
                icon: 'search',
                component: () =>
                    import ('@/views/tables/searchable-table.vue')
            },
            {
                path: 'exportableTable',
                title: '表格导出数据',
                name: 'exportable-table',
                icon: 'code-download',
                component: () =>
                    import ('@/views/tables/exportable-table.vue')
            },
            {
                path: 'table2image',
                title: '表格转图片',
                name: 'table-to-image',
                icon: 'images',
                component: () =>
                    import ('@/views/tables/table-to-image.vue')
            }
        ]
    },
    {
        path: '/advanced-router',
        icon: 'ios-infinite',
        name: 'advanced-router',
        title: '高级路由',
        component: Main,
        children: [{
                path: 'mutative-router',
                title: '动态路由',
                name: 'mutative-router',
                icon: 'link',
                component: () =>
                    import ('@/views/advanced-router/mutative-router.vue')
            },
            {
                path: 'argument-page',
                title: '带参页面',
                name: 'argument-page',
                icon: 'android-send',
                component: () =>
                    import ('@/views/advanced-router/argument-page.vue')
            }
        ]
    },
    {
        path: '/error-page',
        icon: 'android-sad',
        title: '错误页面',
        name: 'errorpage',
        component: Main,
        children: [{
            path: 'index',
            title: '错误页面',
            name: 'errorpage_index',
            component: () =>
                import ('@/views/error-page/error-page.vue')
        }]
    }
];

// 所有上面定义的路由都要写在下面的routers里
export const routers = [
    loginRouter,
    otherRouter,
    preview,
    locking,
    ...appRouter,
    page500,
    page403,
    page404
];