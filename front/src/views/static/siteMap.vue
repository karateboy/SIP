<style lang="less">
@import "../../styles/common.less";
</style>

<template>
    <div>
        <Row>
            <Card>
                <div>
                    <h2>網站地圖  本系統架構說明</h2>
                    <br/>
                    <ol>
                        <li>網站導覽
                            <ol>
                                <li>本站主旨</li>
                                <li>網站地圖</li>
                                <li>隱私權及資訊安全管理</li>  
                            </ol>
                        </li>
                        <li>即時資訊
                            <ol>
                                <li>測站現況</li>
                                <li>即時數據</li>
                                <li>即時小時趨勢圖(當天)</li>  
                            </ol>
                        </li>
                        <li>數據查詢
                            <ol>
                                <li>歷史資料查詢</li>
                                <li>歷史趨勢圖</li>
                                <li>AQI查詢</li>  
                                <li>區間統計</li>
                                <li>相關性統計</li>
                                <li>超過法規值查詢</li>
                                <li>有效數據百分比</li>
                                <li>污染玫瑰圖</li>
                                <li>校正資料查詢</li>
                                <li>警報記錄查詢</li>
                            </ol>
                        </li>
                        <li>監測報表
                            <ol>
                                <li>監測報表</li>
                                <li>空氣品質AQI報表</li>
                                <li>年有效率報表</li>
                                <li>月份時報表</li>
                                <li>測站異常狀況反應表</li>
                                <li>測站每日監測匯總表</li>  
                            </ol>
                        </li>
                        <li>品保驗證
                            <ol>
                                <li>差異驗證</li>
                                <li>即時驗證</li>
                            </ol>
                        </li>
                        <li>系統管理
                            <ol>
                                <li>狀態碼管理</li>
                                <li>測項參數</li>
                                <li>測站管理</li>
                                <li>資料檢核設定</li>
                                <li>使用者管理</li>
                            </ol>
                        </li>
                    </ol>                    
                </div>
            </Card>
        </Row>
    </div>
</template>
<style scoped>
ol > li {
    margin-left: 10px;
}
ol {
  counter-reset: item
}
li {
  display: block
}
li:before {
  content: counters(item, ".") " ";
  counter-increment: item
}
</style>

<script>
export default {
  name: "siteMap",
  data() {
    return {};
  }
};
</script>
