<template>
    <v-app>
        <v-app-bar app elevate-on-scroll absolute>
            <v-btn icon v-if="showAppBarIcon" @click="returnToMain"><v-icon>mdi-arrow-left</v-icon></v-btn>
            <v-toolbar-title v-text="toolbarTitle"></v-toolbar-title>
        </v-app-bar>
        <v-content app>
            <v-divider></v-divider>
            <v-fade-transition mode="out-in">
                <router-view></router-view>
            </v-fade-transition>
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

    export default Vue.extend({
        name: 'App',
        data: () => ({
            showAppBarIcon: false,
            toolbarTitle: "Github代下载服务"
        }),
        mounted() {
            this.$router.beforeEach((to, from, next) => {
                this.$data.showAppBarIcon = to.path == "/about"
                if (to.path == "/about"){
                    this.$data.toolbarTitle = "Github代下载服务 - 关于"
                }else{
                    this.$data.toolbarTitle = "Github代下载服务"
                }
                next()
            })
        },
        methods: {
            returnToMain(){
                this.$router.replace("/")
            }
        }
    });
</script>