<template>
    <div class="row">
        <div class="col-lg-12">
            <div class="ibox ">
                <div class="ibox-content">
                    <div class="map_container">
                        <gmap-map :zoom="1" :center="{lat: 0, lng: 0}" ref="map" class="map_canvas">
                            <gmap-marker v-for="(careHouse, index) in careHouseList"
                                         :key="index"
                                         :clickable="true"
                                         :title="careHouse.name"
                                         icon="/assets/img/green.png"
                                         :position="getPosition(careHouse)"/>
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
        padding-bottom: 42%; /* Ratio 16:9 ( 100%/16*9 = 56.25% ) */
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
                careHouseList: [],
                total: 0,
                display: "",
                dropAnimation: google.maps.Animation.DROP
            }
        },
        mounted: function () {
            this.$gmapDefaultResizeBus.$emit('resize')
            this.fetchCareHouse()
        },
        watch: {
            url: function (newUrl) {
                this.fetchCareHouse(this.skip, this.limit)
            },
            param: function (newParam) {
                this.fetchCareHouse(this.skip, this.limit)
            },
            careHouseList(careHouseList) {
                if (careHouseList.length > 2) {
                    const bounds = new google.maps.LatLngBounds()
                    for (let careHouse of careHouseList) {
                        let pos = this.getPosition(careHouse)
                        bounds.extend(pos)
                    }
                    this.$refs.map.$mapObject.fitBounds(bounds)
                }

            }
        },

        methods: {
            processResp(resp) {
                const ret = resp.data
                this.careHouseList.splice(0, this.careHouseList.length)

                for (let careHouse of ret) {
                    if (careHouse.location && careHouse.location.length == 2)
                        this.careHouseList.push(careHouse)
                }
                console.log("#=" + this.careHouseList.length)
            },
            fetchCareHouse() {
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
            getPosition(careHouse) {
                return {
                    lat: careHouse.location[0],
                    lng: careHouse.location[1]
                }
            }
        },
        components: {}
    }
</script>
