<template>
    <div>
        <!--UPLOAD-->
        <form enctype="multipart/form-data" novalidate>
            <h1>上傳起造人Excel</h1>
            <button type="button" class="btn btn-info" @click.prevent="downloadExcel">下載Excel範本</button>
            <div class="dropbox">
                <input type="file" multiple :name="uploadFieldName" :disabled="isSaving" @change="filesChange($event.target.name, $event.target.files); fileCount = $event.target.files.length" accept=".xlsx" class="input-file">
                <p v-if="isInitial">
                    拖曳Excel至此<br>或點擊上傳
                </p>
                <p v-if="isSaving">
                    上傳 {{ fileCount }} 檔案...
                </p>
            </div>
        </form>
    </div>
</template>
<style>
.dropbox {
    outline: 2px dashed grey;
    /* the dash box */
    outline-offset: -10px;
    background: lightcyan;
    color: dimgray;
    padding: 10px 10px;
    min-height: 200px;
    /* minimum height */
    position: relative;
    cursor: pointer;
}

.input-file {
    opacity: 0;
    /* invisible but it's there! */
    width: 100%;
    height: 200px;
    position: absolute;
    cursor: pointer;
}

.dropbox:hover {
    background: lightblue;
    /* when mouse over to the drop zone, change color */
}

.dropbox p {
    font-size: 1.2em;
    text-align: center;
    padding: 50px 0;
}
</style>
<script>
import axios from 'axios'
import moment from 'moment'
import { mapGetters } from 'vuex'
import baseUrl from "../baseUrl"

const STATUS_INITIAL = 0, STATUS_SAVING = 1;

export default {
    data() {
        return {
            uploadedFiles: [],
            uploadError: null,
            currentStatus: null,
            uploadFieldName: 'buildCase.xlsx'
        }
    },
    computed: {
        isInitial() {
            return this.currentStatus === STATUS_INITIAL;
        },
        isSaving() {
            return this.currentStatus === STATUS_SAVING;
        }
    },
    methods: {
        upload(formData) {
            const url = `${baseUrl()}/UploadBuildCase`;
            return axios.post(url, formData)
                .then(resp => {
                    const ret = resp.data
                    if (ret.Ok)
                        alert("上傳成功")
                })
                .catch(err => alert(err)
                );
        },
        reset() {
            // reset form to initial state
            this.currentStatus = STATUS_INITIAL;
            this.uploadedFiles = [];
            this.uploadError = null;
        },
        save(formData) {
            // upload data to the server
            this.currentStatus = STATUS_SAVING;

            this.upload(formData)
                .then(x => {
                    this.reset()
                })
                .catch(err => {
                    this.uploadError = err.response;
                    this.currentStatus = STATUS_FAILED;
                });
        },
        filesChange(fieldName, fileList) {
            // handle file changes
            const formData = new FormData();

            if (!fileList.length) return;

            // append the files to FormData
            Array
                .from(Array(fileList.length).keys())
                .map(x => {
                    formData.append(fieldName, fileList[x], fileList[x].name);
                });

            // save it
            this.save(formData);
        },
        downloadExcel() {
            let url = baseUrl() + "/BuildCaseTemplate"
            window.open(url)
        }
    },
    mounted() {
        this.reset();
    }
}
</script>
