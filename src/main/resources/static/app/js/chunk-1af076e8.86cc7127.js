(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-1af076e8"],{"0393":function(e,n,t){"use strict";var i=t("5530"),s=(t("0481"),t("210b"),t("604c")),a=t("d9bd");n["a"]=s["a"].extend({name:"v-expansion-panels",provide:function(){return{expansionPanels:this}},props:{accordion:Boolean,disabled:Boolean,flat:Boolean,hover:Boolean,focusable:Boolean,inset:Boolean,popout:Boolean,readonly:Boolean,tile:Boolean},computed:{classes:function(){return Object(i["a"])(Object(i["a"])({},s["a"].options.computed.classes.call(this)),{},{"v-expansion-panels":!0,"v-expansion-panels--accordion":this.accordion,"v-expansion-panels--flat":this.flat,"v-expansion-panels--hover":this.hover,"v-expansion-panels--focusable":this.focusable,"v-expansion-panels--inset":this.inset,"v-expansion-panels--popout":this.popout,"v-expansion-panels--tile":this.tile})}},created:function(){this.$attrs.hasOwnProperty("expand")&&Object(a["a"])("expand","multiple",this),Array.isArray(this.value)&&this.value.length>0&&"boolean"===typeof this.value[0]&&Object(a["a"])(':value="[true, false, true]"',':value="[0, 2]"',this)},methods:{updateItem:function(e,n){var t=this.getValue(e,n),i=this.getValue(e,n+1);e.isActive=this.toggleMethod(t),e.nextIsActive=this.toggleMethod(i)}}})},"210b":function(e,n,t){},"49e2":function(e,n,t){"use strict";var i=t("0789"),s=t("9d65"),a=t("a9ad"),o=t("3206"),r=t("80d2"),c=t("58df"),l=Object(c["a"])(s["a"],a["a"],Object(o["a"])("expansionPanel","v-expansion-panel-content","v-expansion-panel"));n["a"]=l.extend().extend({name:"v-expansion-panel-content",computed:{isActive:function(){return this.expansionPanel.isActive}},created:function(){this.expansionPanel.registerContent(this)},beforeDestroy:function(){this.expansionPanel.unregisterContent()},render:function(e){var n=this;return e(i["a"],this.showLazyContent((function(){return[e("div",n.setBackgroundColor(n.color,{staticClass:"v-expansion-panel-content",directives:[{name:"show",value:n.isActive}]}),[e("div",{class:"v-expansion-panel-content__wrap"},Object(r["p"])(n))])]})))}})},"5a4e":function(e,n,t){e.exports=t.p+"img/all_conditions.c4f6b74f.svg"},9778:function(e,n,t){e.exports=t.p+"img/one_condition.861bd905.svg"},c865:function(e,n,t){"use strict";var i=t("5530"),s=t("0789"),a=t("9d26"),o=t("a9ad"),r=t("3206"),c=t("5607"),l=t("80d2"),p=t("58df"),d=Object(p["a"])(o["a"],Object(r["a"])("expansionPanel","v-expansion-panel-header","v-expansion-panel"));n["a"]=d.extend().extend({name:"v-expansion-panel-header",directives:{ripple:c["a"]},props:{disableIconRotate:Boolean,expandIcon:{type:String,default:"$expand"},hideActions:Boolean,ripple:{type:[Boolean,Object],default:!1}},data:function(){return{hasMousedown:!1}},computed:{classes:function(){return{"v-expansion-panel-header--active":this.isActive,"v-expansion-panel-header--mousedown":this.hasMousedown}},isActive:function(){return this.expansionPanel.isActive},isDisabled:function(){return this.expansionPanel.isDisabled},isReadonly:function(){return this.expansionPanel.isReadonly}},created:function(){this.expansionPanel.registerHeader(this)},beforeDestroy:function(){this.expansionPanel.unregisterHeader()},methods:{onClick:function(e){this.$emit("click",e)},genIcon:function(){var e=Object(l["p"])(this,"actions")||[this.$createElement(a["a"],this.expandIcon)];return this.$createElement(s["c"],[this.$createElement("div",{staticClass:"v-expansion-panel-header__icon",class:{"v-expansion-panel-header__icon--disable-rotate":this.disableIconRotate},directives:[{name:"show",value:!this.isDisabled}]},e)])}},render:function(e){var n=this;return e("button",this.setBackgroundColor(this.color,{staticClass:"v-expansion-panel-header",class:this.classes,attrs:{tabindex:this.isDisabled?-1:null,type:"button","aria-expanded":this.isActive},directives:[{name:"ripple",value:this.ripple}],on:Object(i["a"])(Object(i["a"])({},this.$listeners),{},{click:this.onClick,mousedown:function(){return n.hasMousedown=!0},mouseup:function(){return n.hasMousedown=!1}})}),[Object(l["p"])(this,"default",{open:this.isActive},!0),this.hideActions||this.genIcon()])}})},cd55:function(e,n,t){"use strict";var i=t("5530"),s=t("4e82"),a=t("3206"),o=t("80d2"),r=t("58df");n["a"]=Object(r["a"])(Object(s["a"])("expansionPanels","v-expansion-panel","v-expansion-panels"),Object(a["b"])("expansionPanel",!0)).extend({name:"v-expansion-panel",props:{disabled:Boolean,readonly:Boolean},data:function(){return{content:null,header:null,nextIsActive:!1}},computed:{classes:function(){return Object(i["a"])({"v-expansion-panel--active":this.isActive,"v-expansion-panel--next-active":this.nextIsActive,"v-expansion-panel--disabled":this.isDisabled},this.groupClasses)},isDisabled:function(){return this.expansionPanels.disabled||this.disabled},isReadonly:function(){return this.expansionPanels.readonly||this.readonly}},methods:{registerContent:function(e){this.content=e},unregisterContent:function(){this.content=null},registerHeader:function(e){this.header=e,e.$on("click",this.onClick)},unregisterHeader:function(){this.header=null},onClick:function(e){e.detail&&this.header.$el.blur(),this.$emit("click",e),this.isReadonly||this.isDisabled||this.toggle()},toggle:function(){var e=this;this.content&&(this.content.isBooted=!0),this.$nextTick((function(){return e.$emit("change")}))}},render:function(e){return e("div",{staticClass:"v-expansion-panel",class:this.classes,attrs:{"aria-expanded":String(this.isActive)}},Object(o["p"])(this))}})},d97a:function(e,n,t){"use strict";t.r(n);var i=function(){var e=this,n=e.$createElement,i=e._self._c||n;return i("div",[e.experiment&&e.experiment.conditions?[i("h1",{staticClass:"mb-5"},[i("span",[e._v("You have defined "),i("strong",[e._v(e._s(e.numConditions)+" conditions")])]),i("br"),i("br"),i("span",[e._v("How do you want students to be exposed to these different conditions?")])]),i("v-expansion-panels",{staticClass:"v-expansion-panels--icon",attrs:{flat:""}},[i("v-expansion-panel",{class:{"v-expansion-panel--selected":"WITHIN"===e.experiment.exposureType}},[i("v-expansion-panel-header",{attrs:{"hide-actions":""}},[i("img",{attrs:{src:t("5a4e"),alt:"all conditions"}}),e._v(" All conditions")]),i("v-expansion-panel-content",[i("p",[e._v("All students are exposed to every condition, in different orders. This way you can compare how the different conditions affected each individual student. This is called a within-subject design.")]),i("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:function(n){return e.saveType("WITHIN")}}},[e._v("Select")])],1)],1),i("v-expansion-panel",{class:{"v-expansion-panel--selected":"BETWEEN"===e.experiment.exposureType}},[i("v-expansion-panel-header",{attrs:{"hide-actions":""}},[i("img",{attrs:{src:t("9778"),alt:"only one condition"}}),e._v(" Only one condition")]),i("v-expansion-panel-content",[i("p",[e._v("Each student is only exposed to one condition, so that you can compare how the different conditions affected different students. This is called a between-subjects design.")]),i("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:function(n){return e.saveType("BETWEEN")}}},[e._v("Select")])],1)],1)],1)]:[i("v-alert",{attrs:{prominent:"",type:"error"}},[i("v-row",{attrs:{align:"center"}},[i("v-col",{staticClass:"grow"},[e._v(" No conditions found ")])],1)],1)]],2)},s=[],a=t("5530"),o=t("2f62"),r={name:"ExperimentType",props:["experiment"],computed:{numConditions:function(){var e,n;return(null===(e=this.experiment)||void 0===e||null===(n=e.conditions)||void 0===n?void 0:n.length)||0}},methods:Object(a["a"])(Object(a["a"])({},Object(o["b"])({reportStep:"api/reportStep",updateExperiment:"experiment/updateExperiment"})),{},{saveType:function(e){var n=this,t=this.experiment;t.exposureType=e;var i=t.experimentId,s="exposure_type";this.updateExperiment(t).then((function(e){200===e.status?(n.reportStep({experimentId:i,step:s}),"BETWEEN"===n.experiment.exposureType?n.$router.push({name:"ExperimentDesignDefaultCondition",params:{experiment:i}}):"WITHIN"===n.experiment.exposureType?n.$router.push({name:"ExperimentDesignSummary",params:{experiment:i}}):alert("Select an experiment type")):alert("error: ",e.statusText||e.status)})).catch((function(e){console.log("updateExperiment | catch",{response:e})}))}})},c=r,l=t("2877"),p=t("6544"),d=t.n(p),u=t("0798"),h=t("8336"),v=t("62ad"),x=t("cd55"),f=t("49e2"),m=t("c865"),b=t("0393"),g=t("0fd9"),y=Object(l["a"])(c,i,s,!1,null,null,null);n["default"]=y.exports;d()(y,{VAlert:u["a"],VBtn:h["a"],VCol:v["a"],VExpansionPanel:x["a"],VExpansionPanelContent:f["a"],VExpansionPanelHeader:m["a"],VExpansionPanels:b["a"],VRow:g["a"]})}}]);
//# sourceMappingURL=chunk-1af076e8.86cc7127.js.map