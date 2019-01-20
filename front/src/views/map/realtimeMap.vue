<template>
    <Card>
        <RadioGroup v-model="mapType">
          <Radio label="status">
            <Icon type="logo-apple"></Icon>
            <span>測站狀態</span>
          </Radio>
          <Radio label="wind">
            <Icon type="logo-android"></Icon>
            <span>風場</span>
          </Radio>
        </RadioGroup>
        <div class="map_container">
            <gmap-map :zoom="1" :center="{lat: 0, lng: 0}" ref="map" class="map_canvas">
                <gmap-marker v-for="(monitor, index) in monitorStatusList" :key="index" :clickable="true" :title="monitor.id" :icon="getIcon(monitor)" 
                :position="getPosition(monitor)" @click="toggleInfoWindow(monitor, index)"/>

                <gmap-info-window :options="infoOptions" :position="infoWindowPos" :opened="infoWinOpen" @closeclick="infoWinOpen=false">
                  {{infoContent}}
                </gmap-info-window>
            </gmap-map>
        </div>
    </Card>
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
import axios from "axios";
import { gmapApi } from "vue2-google-maps";

export default {
  data() {
    return {
      monitorStatusList: [],
      mapType: "status",
      currentMidx: 0,
      infoWinOpen: false,
      infoContent: "",
      infoWindowPos: {
        lng: 120.982024,
        lat: 23.973875
      },
      infoOptions: {
        content: "",
        pixelOffset: {
          width: 0,
          height: -35
        }
      }
    };
  },
  computed: {
    google: gmapApi
  },
  created: function() {
    this.$gmapDefaultResizeBus.$emit("resize");
    this.fetchStatus();
  },
  methods: {
    processResp(resp) {
      const ret = resp.data;
      this.monitorStatusList.splice(0, this.monitorStatusList.length);

      for (let status of ret) {
        this.monitorStatusList.push(status);
      }
      setTimeout(this.fitBounds, 1000);
      //this.fitBounds();
    },
    fetchStatus() {
      let url = "/JSON/realtime/map";
      axios
        .get(url)
        .then(this.processResp)
        .catch(err => {
          alert(err);
        });
    },
    getPosition(status) {
      return {
        lat: status.lat,
        lng: status.lng
      };
    },
    getIcon(monitor) {
      if (this.mapType === "status") {
        switch (monitor.status) {
          case 0:
            return "http://icons.iconarchive.com/icons/tatice/cristal-intense/32/ok-icon.png";
          case 1:
            return "http://icons.iconarchive.com/icons/tatice/cristal-intense/32/Chat-vert-icon.png";
          case 2:
            return "http://icons.iconarchive.com/icons/tatice/cristal-intense/32/Chat-rose-icon.png";
          default:
            return "http://icons.iconarchive.com/icons/tatice/cristal-intense/32/Erreur-icon.png";
        }
      } else {
        let icon = {
          path: this.google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
          fillColor: "red",
          strokeColor: "red",
          strokeWeight: 4,
          rotation: 0,
          scale: 4,
          rotation: monitor.winDir
        };
        return icon;
      }
    },
    fitBounds() {
      if (this.monitorStatusList.length > 2) {
        const bounds = new this.google.maps.LatLngBounds();
        for (let status of this.monitorStatusList) {
          let pos = this.getPosition(status);
          bounds.extend(pos);
        }
        this.$refs.map.$mapObject.fitBounds(bounds);
      }
    },
    toggleInfoWindow(m, idx) {
      this.infoWindowPos = this.getPosition(m);

      this.infoOptions.content =
        `<p><strong>${m.id}</strong><br>` +
        `${m.statusStr}<br> 風速:${Math.round(m.winSpeed * 100) / 100 + "m/s"}</p>`;

      //check if its the same marker that was selected if yes toggle
      if (this.currentMidx == idx) {
        this.infoWinOpen = !this.infoWinOpen;
      } else {
        //if different marker set infowindow to open and reset current marker index
        this.infoWinOpen = true;
        this.currentMidx = idx;
      }
    }
  },
  components: {}
};
</script>