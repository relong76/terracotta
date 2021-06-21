(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-ef639f40"],{"0393":function(e,n,t){"use strict";var i=t("5530"),a=(t("0481"),t("210b"),t("604c")),s=t("d9bd");n["a"]=a["a"].extend({name:"v-expansion-panels",provide:function(){return{expansionPanels:this}},props:{accordion:Boolean,disabled:Boolean,flat:Boolean,hover:Boolean,focusable:Boolean,inset:Boolean,popout:Boolean,readonly:Boolean,tile:Boolean},computed:{classes:function(){return Object(i["a"])(Object(i["a"])({},a["a"].options.computed.classes.call(this)),{},{"v-expansion-panels":!0,"v-expansion-panels--accordion":this.accordion,"v-expansion-panels--flat":this.flat,"v-expansion-panels--hover":this.hover,"v-expansion-panels--focusable":this.focusable,"v-expansion-panels--inset":this.inset,"v-expansion-panels--popout":this.popout,"v-expansion-panels--tile":this.tile})}},created:function(){this.$attrs.hasOwnProperty("expand")&&Object(s["a"])("expand","multiple",this),Array.isArray(this.value)&&this.value.length>0&&"boolean"===typeof this.value[0]&&Object(s["a"])(':value="[true, false, true]"',':value="[0, 2]"',this)},methods:{updateItem:function(e,n){var t=this.getValue(e,n),i=this.getValue(e,n+1);e.isActive=this.toggleMethod(t),e.nextIsActive=this.toggleMethod(i)}}})},"0ef2":function(e,n,t){"use strict";t.r(n);var i=function(){var e=this,n=e.$createElement,i=e._self._c||n;return i("div",[e.experiment?[i("h1",{staticClass:"mb-5"},[e._v("How would you like to distribute your experiment participants?")]),i("v-expansion-panels",{staticClass:"v-expansion-panels--icon",attrs:{flat:""}},[i("v-expansion-panel",{class:{"v-expansion-panel--selected":"EVEN"===e.experiment.distributionType}},[i("v-expansion-panel-header",{attrs:{"hide-actions":""}},[i("img",{attrs:{src:t("a3df"),alt:"even distribution"}}),e._v(" Even ")]),i("v-expansion-panel-content",[i("p",[e._v("Equally distribute your students across all conditions")]),i("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:function(n){return e.saveType("EVEN")}}},[e._v("Select")])],1)],1),i("v-expansion-panel",{class:{"v-expansion-panel--selected":"CUSTOM"===e.experiment.distributionType}},[i("v-expansion-panel-header",{attrs:{"hide-actions":""}},[i("img",{attrs:{src:t("ac56"),alt:"custom distribution"}}),e._v(" Custom ")]),i("v-expansion-panel-content",[i("p",[e._v("Customize the percentage of students who receive each condition")]),i("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:function(n){return e.saveType("CUSTOM")}}},[e._v("Select")])],1)],1),i("v-expansion-panel",{class:{"v-expansion-panel--selected":"MANUAL"===e.experiment.distributionType}},[i("v-expansion-panel-header",{attrs:{"hide-actions":""}},[i("img",{attrs:{src:t("1049"),alt:"Manual distribution"}}),e._v(" Manual ")]),i("v-expansion-panel-content",[i("p",[e._v("Manually select which students receive each condition")]),i("v-btn",{attrs:{color:"primary",elevation:"0"},on:{click:function(n){return e.saveType("MANUAL")}}},[e._v("Select")])],1)],1)],1)]:e._e()],2)},a=[],s=t("5530"),o=t("2f62"),r={name:"ParticipationDistribution",props:["experiment"],computed:{},methods:Object(s["a"])(Object(s["a"])({},Object(o["b"])({reportStep:"api/reportStep",updateExperiment:"experiment/updateExperiment"})),{},{saveType:function(e){var n=this,t=this.experiment;t.distributionType=e;var i=t.experimentId,a="distribution_type";this.updateExperiment(t).then((function(e){200===e.status?(n.reportStep({experimentId:i,step:a}),"EVEN"===n.experiment.distributionType?n.$router.push({name:"ParticipationSummary",params:{experiment:i}}):"CUSTOM"===n.experiment.distributionType||"MANUAL"===n.experiment.distributionType||alert("Select a distribution type")):alert("error: ",e.statusText||e.status)})).catch((function(e){console.log("updateExperiment | catch",{response:e})}))}})},c=r,l=t("2877"),p=t("6544"),u=t.n(p),d=t("8336"),h=t("cd55"),v=t("49e2"),x=t("c865"),f=t("0393"),b=Object(l["a"])(c,i,a,!1,null,null,null);n["default"]=b.exports;u()(b,{VBtn:d["a"],VExpansionPanel:h["a"],VExpansionPanelContent:v["a"],VExpansionPanelHeader:x["a"],VExpansionPanels:f["a"]})},1049:function(e,n,t){e.exports=t.p+"img/manual.d9e1c1ea.svg"},"210b":function(e,n,t){},"49e2":function(e,n,t){"use strict";var i=t("0789"),a=t("9d65"),s=t("a9ad"),o=t("3206"),r=t("80d2"),c=t("58df"),l=Object(c["a"])(a["a"],s["a"],Object(o["a"])("expansionPanel","v-expansion-panel-content","v-expansion-panel"));n["a"]=l.extend().extend({name:"v-expansion-panel-content",computed:{isActive:function(){return this.expansionPanel.isActive}},created:function(){this.expansionPanel.registerContent(this)},beforeDestroy:function(){this.expansionPanel.unregisterContent()},render:function(e){var n=this;return e(i["a"],this.showLazyContent((function(){return[e("div",n.setBackgroundColor(n.color,{staticClass:"v-expansion-panel-content",directives:[{name:"show",value:n.isActive}]}),[e("div",{class:"v-expansion-panel-content__wrap"},Object(r["p"])(n))])]})))}})},a3df:function(e,n,t){e.exports=t.p+"img/even.1506b847.svg"},ac56:function(e,n,t){e.exports=t.p+"img/custom.2f619862.svg"},c865:function(e,n,t){"use strict";var i=t("5530"),a=t("0789"),s=t("9d26"),o=t("a9ad"),r=t("3206"),c=t("5607"),l=t("80d2"),p=t("58df"),u=Object(p["a"])(o["a"],Object(r["a"])("expansionPanel","v-expansion-panel-header","v-expansion-panel"));n["a"]=u.extend().extend({name:"v-expansion-panel-header",directives:{ripple:c["a"]},props:{disableIconRotate:Boolean,expandIcon:{type:String,default:"$expand"},hideActions:Boolean,ripple:{type:[Boolean,Object],default:!1}},data:function(){return{hasMousedown:!1}},computed:{classes:function(){return{"v-expansion-panel-header--active":this.isActive,"v-expansion-panel-header--mousedown":this.hasMousedown}},isActive:function(){return this.expansionPanel.isActive},isDisabled:function(){return this.expansionPanel.isDisabled},isReadonly:function(){return this.expansionPanel.isReadonly}},created:function(){this.expansionPanel.registerHeader(this)},beforeDestroy:function(){this.expansionPanel.unregisterHeader()},methods:{onClick:function(e){this.$emit("click",e)},genIcon:function(){var e=Object(l["p"])(this,"actions")||[this.$createElement(s["a"],this.expandIcon)];return this.$createElement(a["c"],[this.$createElement("div",{staticClass:"v-expansion-panel-header__icon",class:{"v-expansion-panel-header__icon--disable-rotate":this.disableIconRotate},directives:[{name:"show",value:!this.isDisabled}]},e)])}},render:function(e){var n=this;return e("button",this.setBackgroundColor(this.color,{staticClass:"v-expansion-panel-header",class:this.classes,attrs:{tabindex:this.isDisabled?-1:null,type:"button","aria-expanded":this.isActive},directives:[{name:"ripple",value:this.ripple}],on:Object(i["a"])(Object(i["a"])({},this.$listeners),{},{click:this.onClick,mousedown:function(){return n.hasMousedown=!0},mouseup:function(){return n.hasMousedown=!1}})}),[Object(l["p"])(this,"default",{open:this.isActive},!0),this.hideActions||this.genIcon()])}})},cd55:function(e,n,t){"use strict";var i=t("5530"),a=t("4e82"),s=t("3206"),o=t("80d2"),r=t("58df");n["a"]=Object(r["a"])(Object(a["a"])("expansionPanels","v-expansion-panel","v-expansion-panels"),Object(s["b"])("expansionPanel",!0)).extend({name:"v-expansion-panel",props:{disabled:Boolean,readonly:Boolean},data:function(){return{content:null,header:null,nextIsActive:!1}},computed:{classes:function(){return Object(i["a"])({"v-expansion-panel--active":this.isActive,"v-expansion-panel--next-active":this.nextIsActive,"v-expansion-panel--disabled":this.isDisabled},this.groupClasses)},isDisabled:function(){return this.expansionPanels.disabled||this.disabled},isReadonly:function(){return this.expansionPanels.readonly||this.readonly}},methods:{registerContent:function(e){this.content=e},unregisterContent:function(){this.content=null},registerHeader:function(e){this.header=e,e.$on("click",this.onClick)},unregisterHeader:function(){this.header=null},onClick:function(e){e.detail&&this.header.$el.blur(),this.$emit("click",e),this.isReadonly||this.isDisabled||this.toggle()},toggle:function(){var e=this;this.content&&(this.content.isBooted=!0),this.$nextTick((function(){return e.$emit("change")}))}},render:function(e){return e("div",{staticClass:"v-expansion-panel",class:this.classes,attrs:{"aria-expanded":String(this.isActive)}},Object(o["p"])(this))}})}}]);
//# sourceMappingURL=chunk-ef639f40.80c4cb7b.js.map