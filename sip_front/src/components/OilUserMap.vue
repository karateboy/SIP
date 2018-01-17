<template>
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox ">
                <div class="ibox-content">
                    <div class="map_container">
                        <gmap-map :zoom="1" :center="{lat: 0, lng: 0}" ref="map" class="map_canvas">
                            <gmap-marker v-for="(oilUser, index) in oilUserList" :key="index" :clickable="true" :title="oilUser.name" :icon="getIcon(oilUser)" :position="getPosition(oilUser)" />
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
            oilUserList: [],
            total: 0,
            display: "",
            dropAnimation: google.maps.Animation.DROP
        }
    },
    mounted: function() {
        this.$gmapDefaultResizeBus.$emit('resize')
        this.fetchOilUser()
    },
    watch: {
        url: function(newUrl) {
            this.fetchOilUser(this.skip, this.limit)
        },
        param: function(newParam) {
            this.fetchOilUser(this.skip, this.limit)
        },
        oilUserList(newList) {
            if (newList.length > 2) {
                const bounds = new google.maps.LatLngBounds()
                for (let oilUser of newList) {
                    let pos = this.getPosition(oilUser)
                    bounds.extend(pos)
                }
                this.$refs.map.$mapObject.fitBounds(bounds)
            }

        }
    },

    methods: {
        processResp(resp) {
            const ret = resp.data
            this.oilUserList.splice(0, this.oilUserList.length)

            for (let oilUser of ret) {
                if (oilUser.location && oilUser.location.length == 2)
                    this.oilUserList.push(oilUser)
            }
        },
        fetchOilUser() {
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
        getPosition(obj) {
            return {
                lat: obj.location[1],
                lng: obj.location[0]
            }
        },
        getIcon(obj) {
            if (obj.useType === "gasStation")
                return "/assets/img/yellow.png"
            else if (obj.useType === "tank")
                return "/assets/img/red.png"
            else
                return "/assets/img/puple.png"

        }
    },
    components: {}
}
</script>