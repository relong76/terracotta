(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-14124b20"],{"3c62":function(t,e,a){"use strict";a("c024")},"75c7":function(t,e,a){"use strict";a.r(e);var n=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[t.experiment?n("v-container",[n("v-row",{staticClass:"my-1",attrs:{justify:"space-between"}},[n("v-col",{attrs:{cols:"8"}},[n("p",{staticClass:"header ma-0 pa-0"},[n("v-img",{staticClass:"mr-6",attrs:{src:a("9fbd"),alt:"Terracotta Logo","max-height":"30","max-width":"27"}}),n("span",[t._v(t._s(t.experiment.title))])],1)])],1),n("v-row",[n("v-col",{attrs:{cols:"12"}},[n("v-divider"),n("v-tabs",{attrs:{elevation:"0"},model:{value:t.tab,callback:function(e){t.tab=e},expression:"tab"}},t._l(t.items,(function(e){return n("v-tab",{key:e},[t._v(" "+t._s(e)+" ")])})),1),n("v-divider",{staticClass:"mb-6"}),n("v-tabs-items",{model:{value:t.tab,callback:function(e){t.tab=e},expression:"tab"}},t._l(t.items,(function(e){return n("v-tab-item",{key:e,staticClass:"py-3"},["status"===e?void 0:t._e(),"setup"===e?[n("v-card",{staticClass:"pt-5 px-5 mx-auto blue lighten-5 rounded-lg",attrs:{outlined:""}},[n("p",{staticClass:"pb-0"},[n("strong",[t._v("Note:")]),t._v(" You are currently collecting assignment submissions. Some setup functionality may be disabled to not disrupt the experiment.")])])]:t._e()],2)})),1)],1)],1)],1):n("v-container",[t._v(" no experiment ")])],1)},s=[],i=a("5530"),r=(a("b0c0"),a("4360")),c=a("2f62"),o={name:"ExperimentSummary",computed:Object(i["a"])({},Object(c["c"])({experiment:"experiment/experiment"})),data:function(){return{tab:null,items:["status","setup"]}},methods:{},created:function(){this.tab="ExperimentSummary"===this.$router.currentRoute.name?1:0},beforeRouteEnter:function(t,e,a){return r["a"].dispatch("experiment/fetchExperimentById",t.params.experiment_id).then(a,a)},beforeRouteUpdate:function(t,e,a){return r["a"].dispatch("experiment/fetchExperimentById",t.params.experiment_id).then(a,a)}},u=o,m=(a("3c62"),a("2877")),l=a("6544"),p=a.n(l),d=a("b0af"),b=a("62ad"),v=a("a523"),f=a("ce7e"),x=a("adda"),h=a("0fd9"),_=a("71a3"),g=a("c671"),y=a("fe57"),w=a("aac8"),C=Object(m["a"])(u,n,s,!1,null,"6b48ff9a",null);e["default"]=C.exports;p()(C,{VCard:d["a"],VCol:b["a"],VContainer:v["a"],VDivider:f["a"],VImg:x["a"],VRow:h["a"],VTab:_["a"],VTabItem:g["a"],VTabs:y["a"],VTabsItems:w["a"]})},"9fbd":function(t,e,a){t.exports=a.p+"img/terracotta_logo_mark.6d713ea8.svg"},c024:function(t,e,a){}}]);
//# sourceMappingURL=chunk-14124b20.3f04bd7a.js.map