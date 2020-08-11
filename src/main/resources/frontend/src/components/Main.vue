<template>
    <v-container style="height: 100%">
        <v-row align="center" justify="center" style="height: 100%">
            <v-col cols="10" sm="7" md="7" lg="6" xl="6">
                <v-text-field
                        shaped
                        :loading="$store.state.theState !== 0
                        && $store.state.theState !== 5"
                        :disabled="$store.state.textFieldDisabled"
                        filled label="在此输入URL" v-model="$store.state.urlToDownload"
                        prepend-inner-icon="mdi-github"
                        append-icon="mdi-help-circle-outline"
                        @click:append="help = true">
                    <template v-slot:progress>
                        <v-progress-linear
                                absolute
                                v-if="$store.state.theState === 3"
                                :value="$store.state.progress"
                        ></v-progress-linear>
                    </template>
                </v-text-field>
                <v-row dense align="center" justify="center">
                    <v-btn v-if="!$store.state.readyToDownload"
                           text outlined @click="getDownloadLink"
                           v-text="btnText" :disabled="$store.state.btnDisabled"></v-btn>
                    <v-btn v-if="$store.state.readyToDownload" text outlined target="_blank"
                           :href="$store.state.downloadUrl">已完成，点此下载
                    </v-btn>
                    <br><br>
                    <v-btn v-if="$store.state.readyToDownload" text outlined @click="reset" v-text="resetBtn"
                           style="margin-left: 8px"></v-btn>
                </v-row>
            </v-col>
        </v-row>
        <v-snackbar color="red" light v-model="snackbar">URL不能为空</v-snackbar>
        <v-dialog persistent v-model="$store.state.disconnected" max-width="290">
            <v-card>
                <v-card-title>与服务器断开连接</v-card-title>
                <v-card-text>请与柠喵联系，点击确认来刷新页面</v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn @click="$router.go(0)" text>好</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
        <v-dialog persistent v-model="$store.state.failed" max-width="290">
            <v-card>
                <v-card-title>错误发生</v-card-title>
                <v-card-text v-text="errorMessage"></v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn @click="reset" text>好</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
        <v-dialog v-model="help" max-width="550">
            <v-card>
                <v-card-title>帮助</v-card-title>
                <v-card-text>
                    Q：仅限Github资源吗？<br>
                    A：是的，因为做这个站的初衷就是为了能更快地下载Github源码<br>
                    Q：能下载Release和Archive包吗？<br>
                    A：可以，毕竟Release和Archive包的下载速度也很慢<br>
                    <br>
                    注意，请不要下载过大的资源，目前这个站的月可用流量很低qwq</v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn @click="help = false" text>好</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-container>
</template>

<script lang="ts">
    import Vue from "vue"
    import {State} from "@/store";

    export default Vue.extend({
        name: "Main",
        data: () => ({
            snackbar: false,
            btnText: "开始",
            resetBtn: "重置状态",
            errorMessage: "",
            help: false
        }),
        mounted() {
            this.$store.state.messageListeners.add(this.onMessage)
            this.$store.state.closeListeners.add(this.onClose)
        },
        methods: {
            getDownloadLink() {
                const url = this.$store.state.urlToDownload
                if (url == "") {
                    this.$data.snackbar = true
                    setTimeout(() => this.$data.snackbar = false, 1500)
                    return
                }
                const ws = this.$store.state.ws
                ws.send(url)
            },
            onMessage(ws: WebSocket, e: MessageEvent) {
                if (process.env.VUE_APP_MODE == "development") {
                    console.log(e.data)
                }
                const message = JSON.parse(e.data)
                if (message.status == "parsing") {
                    this.$data.btnText = "正在解析"
                    this.$store.state.textFieldDisabled = true
                    this.$store.state.btnDisabled = true
                    this.$store.state.theState = State.PARSING
                } else if (message.status == "downloading") {
                    this.$data.btnText = "正在远程下载"
                    this.$store.state.theState = State.DOWNLOADING
                    this.$store.state.progress = message.text
                } else if (message.status == "checking out") {
                    this.$data.btnText = "正在检出代码"
                    this.$store.state.theState = State.CHECKING_OUT
                } else if (message.status == "compressing") {
                    this.$data.btnText = "正在打包"
                    this.$store.state.theState = State.COMPRESSING
                } else if (message.status == "completed") {
                    this.$store.state.readyToDownload = true
                    this.$store.state.theState = State.COMPLETED
                    let prefix = ""
                    if (process.env.VUE_APP_MODE == "development") {
                        prefix = "http://localhost:4000/file?fileName="
                    } else {
                        prefix = "http://8.210.48.126:4000/file?fileName="
                    }
                    this.$store.state.downloadUrl = prefix + message.text
                } else if (message.status == "error"){
                    this.$store.state.failed = true
                    this.$data.errorMessage = message.text
                }
            },
            reset() {
                this.$data.btnText = "开始"
                this.$store.state.btnDisabled = false
                this.$store.state.textFieldDisabled = false
                this.$store.state.urlToDownload = ""
                this.$store.state.readyToDownload = false
                this.$store.state.theState = State.READY
                this.$store.state.unsupported = false
                this.$store.state.failed = false
            },
            onClose(ws: WebSocket, e: CloseEvent) {
                this.$store.state.disconnected = true
            }
        }
    })
</script>

<style scoped>

</style>