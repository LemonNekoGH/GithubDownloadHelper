<template>
    <v-app>
        <v-app-bar app elevate-on-scroll>
            <v-btn icon v-if="$store.state.showAppBarIcon" @click="returnToMain"><v-icon>mdi-arrow-left</v-icon></v-btn>
            <v-toolbar-title v-text="toolbarTitle"></v-toolbar-title>
        </v-app-bar>
        <v-content app>
            <v-divider></v-divider>
            <v-fade-transition mode="out-in">
                <router-view></router-view>
            </v-fade-transition>
            <v-snackbar v-model="snackbar" color="orange">已切换到后台继续进行</v-snackbar>
        </v-content>
        <v-footer>
            <div></div>
            <v-spacer></v-spacer>
            <div>&copy;&nbsp;{{new Date().getFullYear()}} | <router-link style="text-decoration: none" to="/about">关于本站</router-link></div>
        </v-footer>
    </v-app>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {State} from "@/store";

    export default Vue.extend({
        name: 'App',
        data: () => ({
            toolbarTitle: "Github代下载服务",
            snackbar: false
        }),
        mounted() {
            this.$router.beforeEach((to, from, next) => {
                this.$store.state.showAppBarIcon = to.path == "/about"
                if (to.path == "/about"){
                    this.$data.toolbarTitle = "Github代下载服务 - 关于"
                    if (this.$store.state.theState != State.READY &&
                        this.$store.state.theState != State.COMPLETED){
                        this.$data.snackbar = true
                        setTimeout(() => this.$data.snackbar = false,3000)
                    }
                }else{
                    this.$data.toolbarTitle = "Github代下载服务"
                }
                next()
            })
            window.onbeforeunload = () => {
                if (this.$store.state.theState !== 0 &&
                this.$store.state.theState !== 5 &&
                this.$store.state.theState !== 6){
                    return window.confirm()
                }
            }
        },
        methods: {
            returnToMain(){
                this.$router.replace("/")
            }
        }
    });
</script>