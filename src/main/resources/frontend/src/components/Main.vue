<template>
    <v-container style="height: 100%">
        <v-row align="center" justify="center" style="height: 100%">
            <v-col cols="6">
                <v-text-field filled label="在此输入URL" v-model="urlToDownload"
                              prepend-inner-icon="mdi-github" shaped></v-text-field>
                <v-row dense align="center" justify="center">
                    <v-btn v-if="!readyToDownload" text outlined @click="getDownloadLink" v-text="btnText"></v-btn>
                    <v-btn v-if="readyToDownload" text outlined :href="downloadUrl">打包完成，点此下载</v-btn>
                </v-row>
            </v-col>
        </v-row>
        <v-snackbar color="red" light v-model="snackbar">URL不能为空</v-snackbar>
    </v-container>
</template>

<script lang="ts">
    import Vue from "vue"
    import {AxiosError, AxiosResponse} from "axios";
    export default Vue.extend( {
        name: "Main",
        data: () => ({
            urlToDownload: "",
            snackbar: false,
            btnText: "开始",
            readyToDownload: false,
            downloadUrl: ""
        }),
        methods: {
            getDownloadLink() {
                const axios = require("axios").default
                if (this.$data.readyToDownload){
                    axios.get("/api/file?fileName=" + this.$data.downloadUrl,{
                        responseType: "arraybuffer"
                    }).then((res: AxiosResponse) => {
                        if (res.status == 200){
                            window.location.href=URL.createObjectURL(new Blob([res.data.fileName], {type: "x-zip-compressed"}))
                        }
                    }).catch((e: AxiosError) => {
                        console.log(e.message)
                    })
                    return
                }

                function checking(url: String, _this: Vue) {
                    axios.get("/api/checkout?url=" + url)
                        .then((res: AxiosResponse) => {
                            let status = res.data.status
                            if (status == "checking out") {
                                _this.$data.btnText = "正在检出代码"
                                setTimeout(() => checking(url, _this), 2000)
                            } else if (status == "compressing") {
                                _this.$data.btnText = "正在打包"
                                setTimeout(() => checking(url, _this),2000)
                            } else if (status == "completed"){
                                _this.$data.readyToDownload = true
                                _this.$data.downloadUrl = "/api/file?fileName=" + res.data.fileName
                            }
                        }).catch((e: AxiosError) => {
                        console.log(e.message)
                    })
                }
                checking(this.$data.urlToDownload,this)
            }
        }
    })
</script>

<style scoped>

</style>