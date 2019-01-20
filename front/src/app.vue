<template>
    <div id="main" class="app-main">
        <router-view></router-view>
    </div>
</template>

<script>
    export default {
        data () {
            return {
                theme: this.$store.state.app.themeColor
            };
        },
        mounted () {

        },
        beforeDestroy () {

        },
        methods: {

        }
    };
</script>

<style>
html,body{
    width: 100%;
    height: 100%;
    background: #f0f0f0;
    overflow: hidden;
}
.app-main{
    width: 100%;
    height: 100%;
}

.normal {
  color: black;
}

.over_internal_std {
  color: DarkBlue;
}

.over_law_std {
  color: DarkRed;
}

.calibration_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#b4e391+0,61c419+50,b4e391+100;Green+3D */
  background: #b4e391; /* Old browsers */
  background: -moz-linear-gradient(
    top,
    #b4e391 0%,
    #61c419 50%,
    #b4e391 100%
  ); /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #b4e391 0%,
    #61c419 50%,
    #b4e391 100%
  ); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #b4e391 0%,
    #61c419 50%,
    #b4e391 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#b4e391', endColorstr='#b4e391',GradientType=0 ); /* IE6-9 */
}
.maintain_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#e570e7+0,c85ec7+47,a849a3+100;Pink+3D+%233 */
  background: #e570e7; /* Old browsers */
  background: -moz-linear-gradient(top, #e570e7 0%, #c85ec7 47%, #a849a3 100%);
  /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #e570e7 0%,
    #c85ec7 47%,
    #a849a3 100%
  ); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #e570e7 0%,
    #c85ec7 47%,
    #a849a3 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#e570e7', endColorstr='#a849a3',GradientType=0 ); /* IE6-9 */
}
.ivu-table .abnormal_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#ff3019+0,cf0404+100;Red+3D */
  background: #ff3019; /* Old browsers */
  background: -moz-linear-gradient(
    top,
    #ff3019 0%,
    #cf0404 100%
  ); /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #ff3019 0%,
    #cf0404 100%
  ); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #ff3019 0%,
    #cf0404 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ff3019', endColorstr='#cf0404',GradientType=0 ); /* IE6-9 */
}
.auto_audit_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#d0e4f7+0,73b1e7+24,0a77d5+50,539fe1+79,87bcea+100;Blue+Pipe+%231 */
  background: #d0e4f7; /* Old browsers */
  background: -moz-linear-gradient(
    top,
    #d0e4f7 0%,
    #73b1e7 24%,
    #0a77d5 50%,
    #539fe1 79%,
    #87bcea 100%
  );
  /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #d0e4f7 0%,
    #73b1e7 24%,
    #0a77d5 50%,
    #539fe1 79%,
    #87bcea 100%
  );
  /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #d0e4f7 0%,
    #73b1e7 24%,
    #0a77d5 50%,
    #539fe1 79%,
    #87bcea 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#d0e4f7', endColorstr='#87bcea',GradientType=0 );
  /* IE6-9 */
}
.manual_audit_status {
  /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#f1e767+0,feb645+100;Yellow+3D */
  background: #f1e767; /* Old browsers */
  background: -moz-linear-gradient(top, #f1e767 0%, #feb645 100%);
  /* FF3.6-15 */
  background: -webkit-linear-gradient(
    top,
    #f1e767 0%,
    #feb645 100%
  ); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(
    to bottom,
    #f1e767 0%,
    #feb645 100%
  ); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#f1e767', endColorstr='#feb645',GradientType=0 ); /* IE6-9 */
}
</style>
