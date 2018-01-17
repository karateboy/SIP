<<template>
    <build-case-detail :buildCase="buildCase"></build-case-detail>
</template>
<style scoped>

</style>
<<script>
import BuildCaseDetail from './BuildCaseDetail.vue'

export default {
    data() {
        return {
            buildCase:{
                _id: "", 
                county: "", 
                name: "",
                architect: "", 
                area: 0, 
                addr: "", 
                date: new Date(),
                location: [0,0],
                contracted: false
            }
        }
    },
    components:{
        BuildCaseDetail
    }
}
</script>

