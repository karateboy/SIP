<template>
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox ">
                <div class="ibox-content">
                    <div class="map_container">
                        <gmap-map :zoom="1" :center="{lat: 0, lng: 0}" ref="map" class="map_canvas">
                            <gmap-marker v-for="(buildCase, index) in buildCaseList" :key="index" :clickable="true" :title="buildCase.name" :icon="getIcon(buildCase)" :position="getPosition(buildCase)" />
                        </gmap-map>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<style>
.map_container {
    position: relative;
    width: 100%;
    padding-bottom: 42%;
    /* Ratio 16:9 ( 100%/16*9 = 56.25% ) */
}

.map_container .map_canvas {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    margin: 0;
    padding: 0;
}
</style>
<script>
import axios from 'axios'

export default {
    props: {
        url: {
            type: String,
            required: true
        },
        param: {
            type: Object
        }
    },
    data() {
        return {
            buildCaseList: [],
            total: 0,
            display: "",
            dropAnimation: google.maps.Animation.DROP
        }
    },
    mounted: function() {
        this.$gmapDefaultResizeBus.$emit('resize')
        this.fetchBuildCase()
    },
    watch: {
        url: function(newUrl) {
            this.fetchBuildCase(this.skip, this.limit)
        },
        param: function(newParam) {
            this.fetchBuildCase(this.skip, this.limit)
        },
        buildCaseList(buildCaseList) {
            if (buildCaseList.length > 2) {
                const bounds = new google.maps.LatLngBounds()
                for (let buildCase of buildCaseList) {
                    let pos = this.getPosition(buildCase)
                    bounds.extend(pos)
                }
                this.$refs.map.$mapObject.fitBounds(bounds)
            }

        }
    },

    methods: {
        processResp(resp) {
            const ret = resp.data
            this.buildCaseList.splice(0, this.buildCaseList.length)

            for (let buildCase of ret) {
                if (buildCase.location && buildCase.location.length == 2)
                    this.buildCaseList.push(buildCase)
            }
            console.log("#=" + this.buildCaseList.length)
        },
        fetchBuildCase() {
            let request_url = `${this.url}`

            if (this.param) {
                axios.post(request_url, this.param).then(this.processResp).catch((err) => {
                    alert(err)
                })
            } else {
                axios.get(request_url).then(this.processResp).catch((err) => {
                    alert(err)
                })
            }
        },
        getPosition(buildCase) {
            return {
                lat: buildCase.location[1],
                lng: buildCase.location[0]
            }
        },
        getIcon(buildCase) {
            let sentDate = moment(buildCase.date)
            let now = moment()
            let yellowDue = sentDate.add(4, "month")
            let redDue = yellowDue.add(2, "month")
            if (now.isBefore(yellowDue))
                return "/assets/img/yellow.png"
            else if (now.isAfter(yellowDue) && now.isBefore(redDue))
                return "/assets/img/red.png"
            else if(now.isAfter(redDue))
                return "/assets/img/puple.png"

        }
    },
    components: {}
}
</script>