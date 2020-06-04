<template>
    <v-container>
        <v-row justify="center">
            <v-col cols="6">
                <v-card outlined>
                    <v-card-subtitle>免责声明</v-card-subtitle>
                    <v-divider></v-divider>
                    <v-card-text>
                        当你点击“开始”按钮时，视为对以下声明内容的全部认可：<br>
                        1. 本站不储存资源（缓存除外），只是中转平台<br>
                        2. 使用本站下载任何资源都需要自行承担风险<br>
                        3. 本站仅仅只是帮你下载网上的代码，所以无法对这些代码的可用性、准确性或可靠性做出任何承诺和保证<br>
                        4. 如果使用本站下载的代码造成了损失，本站不承担任何责任<br>
                        5. 任何单位和个人不得使用本站下载侵权、盗版、违法的任何软件或资料<br>
                        6. 本声明的最终解释权归本站归属人所有（同样适用于用本站源码搭建代下载服务的其它站长）
                        <br>
                    </v-card-text>
                </v-card>
            </v-col>
            <v-col cols="6">
                <v-card outlined>
                    <v-card-subtitle>关于本站</v-card-subtitle>
                    <v-divider></v-divider>
                    <v-card-text>
                        顾名思义，就是用来帮你下载Github上的项目的服务，当你的clone速度太慢时可以尝试一下用这个下载。
                        这个项目的最初版本由 Github@xiaoxinda 使用PHP实现，柠喵想要为这个服务搭建备用节点时因为看不懂PHP而使用Java重新实现。
                        和原项目一样，这个项目同样在Github上开源。
                    </v-card-text>
                    <v-divider></v-divider>
                    <v-card-actions>
                        <v-btn text width="100%">
                            <a style="text-decoration: none" href="https://github.com/LemonNekoGH/Github-Proxy-Java">前往Github</a>
                        </v-btn>
                    </v-card-actions>
                    <v-divider></v-divider>
                    <v-card-actions>
                        <v-btn text width="100%">
                            <a style="text-decoration: none" href="https://space.bilibili.com/5325421">前往柠喵的B站空间</a>
                        </v-btn>
                    </v-card-actions>
                    <v-divider></v-divider>
                    <v-card-actions>
                        <v-btn text width="100%">
                            <a style="text-decoration: none" href="https://afdian.net/@TAGP0">为柠喵发电</a>
                        </v-btn>
                    </v-card-actions>
                </v-card>
            </v-col>
        </v-row>
        <v-snackbar v-model="snackbar">操作已经顺利完成，可以下载了</v-snackbar>
    </v-container>
</template>

<script lang="ts">
    import Vue from "vue"
    export default Vue.extend({
        name: "About",
        data: () => ({
            snackbar: false
        }),
        mounted() {
            const _this = this
            this.$store.state.messageListeners.add(function (ws: WebSocket, e: MessageEvent) {
                if (e.data == "completed"){
                    _this.$data.snackbar = true
                    setTimeout(() => _this.$data.snackbar = false, 3000)
                }
            })
        }
    })
</script>