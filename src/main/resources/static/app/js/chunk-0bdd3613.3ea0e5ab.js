(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-0bdd3613"],{"5a4e":function(e,n,t){e.exports=t.p+"img/all_conditions.c4f6b74f.svg"},9778:function(e,n,t){e.exports=t.p+"img/one_condition.861bd905.svg"},d97a:function(e,n,t){"use strict";t.r(n);var i=function(){var e=this,n=e.$createElement,i=e._self._c||n;return i("div",[e.experiment&&e.experiment.conditions?[i("h1",{staticClass:"mb-5"},[i("span",[e._v("You have defined "),i("strong",[e._v(e._s(e.numConditions)+" conditions")])]),i("br"),i("br"),i("span",[e._v("How do you want students to be exposed to these different conditions?")])]),i("v-expansion-panels",{staticClass:"v-expansion-panels--icon",attrs:{flat:""}},[i("v-expansion-panel",{class:{"v-expansion-panel--selected":"BETWEEN"===e.experiment.exposureType}},[i("v-expansion-panel-header",{attrs:{"hide-actions":""}},[i("img",{attrs:{src:t("5a4e"),alt:"all conditions"}}),e._v(" All conditions")]),i("v-expansion-panel-content",[i("p",[e._v("All students are exposed to every condition, in different orders. This way you can compare how the different conditions affected each individual student. This is called a within-subject design.")]),i("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:function(n){return e.saveType("BETWEEN")}}},[e._v("Select")])],1)],1),i("v-expansion-panel",{class:{"v-expansion-panel--selected":"WITHIN"===e.experiment.exposureType}},[i("v-expansion-panel-header",{attrs:{"hide-actions":""}},[i("img",{attrs:{src:t("9778"),alt:"only one condition"}}),e._v(" Only one condition")]),i("v-expansion-panel-content",[i("p",[e._v("Each student is only exposed to one condition, so that you can compare how the different conditions affected different students. This is called a between-subjects design.")]),i("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:function(n){return e.saveType("WITHIN")}}},[e._v("Select")])],1)],1)],1)]:[i("v-alert",{attrs:{prominent:"",type:"error"}},[i("v-row",{attrs:{align:"center"}},[i("v-col",{staticClass:"grow"},[e._v(" No conditions found ")])],1)],1)]],2)},o=[],s=t("5530"),a=t("2f62"),r={name:"ExperimentType",props:["experiment"],computed:{numConditions:function(){return this.experiment.conditions.length}},methods:Object(s["a"])(Object(s["a"])({},Object(a["b"])({updateExperiment:"experiment/updateExperiment"})),{},{saveType:function(e){var n=this,t=this,i=t.experiment;i.exposureType=e,this.updateExperiment(i).then((function(e){200===e.status?"WITHIN"===n.experiment.exposureType?n.$router.push({name:"ExperimentDesignDefaultCondition",params:{experiment:n.experiment.experiment_id}}):"BETWEEN"===n.experiment.exposureType&&n.$router.push({name:"ExperimentDesignSummary",params:{experiment:n.experiment.experiment_id}}):alert("error: ",e.statusText||e.status)})).catch((function(e){console.log("updateExperiment | catch",{response:e})}))}})},p=r,c=t("2877"),d=t("6544"),l=t.n(d),u=t("0798"),x=t("8336"),m=t("62ad"),v=t("cd55"),f=t("49e2"),h=t("c865"),E=t("0393"),y=t("0fd9"),b=Object(c["a"])(p,i,o,!1,null,null,null);n["default"]=b.exports;l()(b,{VAlert:u["a"],VBtn:x["a"],VCol:m["a"],VExpansionPanel:v["a"],VExpansionPanelContent:f["a"],VExpansionPanelHeader:h["a"],VExpansionPanels:E["a"],VRow:y["a"]})}}]);
//# sourceMappingURL=chunk-0bdd3613.3ea0e5ab.js.map