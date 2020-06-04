<template>
    <v-container style="height: 100%">
        <v-row align="center" justify="center" style="height: 100%">
            <v-col cols="10" sm="7" md="7" lg="6" xl="6">
                <v-text-field
                        :loading="$store.state.theState === 1 || $store.state.theState === 2"
                        :disabled="$store.state.textFieldDisabled"
                              filled label="在此输入URL" v-model="$store.state.urlToDownload"
                              prepend-inner-icon="mdi-github" shaped></v-text-field>
                <v-row dense align="center" justify="center">
                    <v-btn v-if="!$store.state.readyToDownload"
                           text outlined @click="getDownloadLink"
                           v-text="btnText" :disabled="$store.state.btnDisabled"></v-btn>
                    <v-btn v-if="$store.state.readyToDownload" text outlined :href="$store.state.downloadUrl">打包完成，点此下载</v-btn><br><br>
                    <v-btn v-if="$store.state.readyToDownload" text outlined @click="reset" v-text="resetBtn" style="margin-left: 8px"></v-btn>
                </v-row>
            </v-col>
        </v-row>
        <v-snackbar color="red" light v-model="snackbar" >URL不能为空</v-snackbar>
    </v-container>
</template>

<script lang="ts">
    import Vue from "vue"
    import {State} from "@/store";

    export default Vue.extend( {
        name: "Main",
        data: () => ({
            snackbar: false,
            btnText: "开始",
            resetBtn: "重置状态"
        }),
        mounted() {
            this.$store.state.messageListeners.add(this.onMessage)
        },
        methods: {
            getDownloadLink() {
                const url = this.$store.state.urlToDownload
                if (url == ""){
                    this.$data.snackbar = true
                    setTimeout(() => this.$data.snackbar = false, 1500)
                    return
                }
                const ws = this.$store.state.ws
                ws.send(url)
            },
            onMessage(ws: WebSocket,e: MessageEvent){
                if (e.data == "start checking"){
                    this.$data.btnText = "正在检出代码"
                    this.$store.state.textFieldDisabled = true
                    this.$store.state.btnDisabled = true
                    this.$store.state.theState = State.CHECKING_OUT
                }else if (e.data == "start compressing"){
                    this.$data.btnText = "正在打包"
                    this.$store.state.theState = State.COMPRESSING
                }else if (e.data == "completed"){
                    this.$store.state.readyToDownload = true
                    this.$store.state.theState = State.COMPLETED
                }else{
                    let prefix = ""
                    if (process.env.VUE_APP_MODE == "development"){
                        prefix = "http://localhost:4000/file?fileName="
                    }else{
                        prefix = "http://45.32.228.179:4000/file?fileName="
                    }
                    this.$store.state.downloadUrl = prefix + e.data
                }
            },
            reset(){
                this.$data.btnText = "开始"
                this.$store.state.btnDisabled = false
                this.$store.state.textFieldDisabled = false
                this.$store.state.urlToDownload = ""
                this.$store.state.readyToDownload = false
                this.$store.state.theState = State.READY
            }
        }
    })
</script>

<style scoped>

</style>