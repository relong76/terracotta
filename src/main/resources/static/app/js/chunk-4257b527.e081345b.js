(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-4257b527"],{"7f79":function(e,t,n){"use strict";n.r(t);var i=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",[e.experiment?[n("div",{staticClass:"experiment-steps"},[n("aside",{staticClass:"experiment-steps__sidebar"},[n("steps",{attrs:{"current-section":e.currentSection,"current-step":e.currentStep}})],1),n("nav",[e.$routerHistory.hasPrevious()?n("router-link",{attrs:{to:{path:e.$routerHistory.previous().path}}},[n("v-icon",[e._v("mdi-chevron-left")]),e._v(" Back ")],1):e._e()],1),n("article",{staticClass:"experiment-steps__body"},[n("v-container",[n("v-row",{attrs:{justify:"center"}},[n("v-col",{attrs:{md:"6"}},[n("router-view",{key:e.$route.fullPath,attrs:{experiment:e.experiment}})],1)],1)],1)],1)])]:[n("v-row",{attrs:{justify:"center"}},[n("v-col",{attrs:{md:"6"}},[n("v-alert",{attrs:{prominent:"",type:"error"}},[n("v-row",{attrs:{align:"center"}},[n("v-col",{staticClass:"grow"},[e._v(" Experiment not found ")])],1)],1)],1)],1)]],2)},s=[],r=n("5530"),a=(n("b0c0"),function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("ul",{staticClass:"component-steps"},e._l(e.sectionList,(function(t){return n("li",{key:t.key},[t.key===e.currentSection?[n("strong",{class:{"green--text":e.isSummary&&t.key===e.$route.meta.currentSection}},[e._v(e._s(t.name))])]:[n("span",{class:{"green--text":e.isSummary&&t.key===e.$route.meta.currentSection}},[e._v(e._s(t.name))])],t.key===e.currentSection?n("v-stepper",{class:{finished:e.isSummary},attrs:{vertical:""}},e._l(t.steps,(function(i){return n("v-stepper-step",{key:i.key,attrs:{complete:e.isSummary?e.isSummary:t.steps.findIndex((function(e){return e.key===i.key}))<=e.currentStepNum,step:""}},[e._v(" "+e._s(i.name)+" ")])})),1):e._e()],2)})),0)}),o=[],c=(n("c740"),n("4de4"),{name:"Steps",props:["currentSection","currentStep"],computed:{isSummary:function(){return"ExperimentDesignSummary"===this.$route.name||"ParticipationSummary"===this.$route.name},currentStepNum:function(){var e=this;return this.sectionList.filter((function(t){return t.key===e.currentSection}))[0].steps.findIndex((function(t){return t.key===e.currentStep}))}},methods:{generateSteps:function(){var e=this.$route.meta.selectionType,t=[{key:"participation_selection_method",name:"Selection Method"}];return"consent"===e?t.push({key:"participation_selection_consent_title",name:"Assignment Title"},{key:"participation_selection_consent_file",name:"Informed Consent"}):"manual"===e&&t.push({key:"select_participants",name:"Select Participants"}),"any"!==e&&"consent"!==e&&"manual"!==e||t.push({key:"participation_distribution",name:"Distribution"}),t}},data:function(){return{sectionList:[{key:"design",name:"Section 1: Design",steps:[{key:"design_title",name:"Title"},{key:"design_description",name:"Description"},{key:"design_conditions",name:"Conditions"},{key:"design_type",name:"Experiment type"}]},{key:"participation",name:"Section 2: Participation",steps:this.generateSteps()},{key:"assignments",name:"Section 3: Assignments",steps:[{key:"assignment_intro",name:"Introduction"}]}]}}}),p=c,u=(n("f8b9"),n("2877")),l=n("6544"),h=n.n(l),m=(n("a9e3"),n("8836"),n("3206")),d=n("a452"),f=n("7560"),v=n("58df"),y=n("d9bd"),_=Object(v["a"])(Object(m["b"])("stepper"),d["a"],f["a"]),g=_.extend({name:"v-stepper",provide:function(){return{stepClick:this.stepClick,isVertical:this.vertical}},props:{altLabels:Boolean,nonLinear:Boolean,vertical:Boolean},data:function(){var e={isBooted:!1,steps:[],content:[],isReverse:!1};return e.internalLazyValue=null!=this.value?this.value:(e[0]||{}).step||1,e},computed:{classes:function(){return Object(r["a"])({"v-stepper--is-booted":this.isBooted,"v-stepper--vertical":this.vertical,"v-stepper--alt-labels":this.altLabels,"v-stepper--non-linear":this.nonLinear},this.themeClasses)}},watch:{internalValue:function(e,t){this.isReverse=Number(e)<Number(t),t&&(this.isBooted=!0),this.updateView()}},created:function(){this.$listeners.input&&Object(y["a"])("@input","@change",this)},mounted:function(){this.updateView()},methods:{register:function(e){"v-stepper-step"===e.$options.name?this.steps.push(e):"v-stepper-content"===e.$options.name&&(e.isVertical=this.vertical,this.content.push(e))},unregister:function(e){"v-stepper-step"===e.$options.name?this.steps=this.steps.filter((function(t){return t!==e})):"v-stepper-content"===e.$options.name&&(e.isVertical=this.vertical,this.content=this.content.filter((function(t){return t!==e})))},stepClick:function(e){var t=this;this.$nextTick((function(){return t.internalValue=e}))},updateView:function(){for(var e=this.steps.length;--e>=0;)this.steps[e].toggle(this.internalValue);for(var t=this.content.length;--t>=0;)this.content[t].toggle(this.internalValue,this.isReverse)}},render:function(e){return e("div",{staticClass:"v-stepper",class:this.classes},this.$slots.default)}}),S=(n("d3b7"),n("25f0"),n("9d26")),b=n("a9ad"),k=n("5607"),x=Object(v["a"])(b["a"],Object(m["a"])("stepper","v-stepper-step","v-stepper")),$=x.extend().extend({name:"v-stepper-step",directives:{ripple:k["a"]},inject:["stepClick"],props:{color:{type:String,default:"primary"},complete:Boolean,completeIcon:{type:String,default:"$complete"},editable:Boolean,editIcon:{type:String,default:"$edit"},errorIcon:{type:String,default:"$error"},rules:{type:Array,default:function(){return[]}},step:[Number,String]},data:function(){return{isActive:!1,isInactive:!0}},computed:{classes:function(){return{"v-stepper__step--active":this.isActive,"v-stepper__step--editable":this.editable,"v-stepper__step--inactive":this.isInactive,"v-stepper__step--error error--text":this.hasError,"v-stepper__step--complete":this.complete}},hasError:function(){return this.rules.some((function(e){return!0!==e()}))}},mounted:function(){this.stepper&&this.stepper.register(this)},beforeDestroy:function(){this.stepper&&this.stepper.unregister(this)},methods:{click:function(e){e.stopPropagation(),this.$emit("click",e),this.editable&&this.stepClick(this.step)},genIcon:function(e){return this.$createElement(S["a"],e)},genLabel:function(){return this.$createElement("div",{staticClass:"v-stepper__label"},this.$slots.default)},genStep:function(){var e=!(this.hasError||!this.complete&&!this.isActive)&&this.color;return this.$createElement("span",this.setBackgroundColor(e,{staticClass:"v-stepper__step__step"}),this.genStepContent())},genStepContent:function(){var e=[];return this.hasError?e.push(this.genIcon(this.errorIcon)):this.complete?this.editable?e.push(this.genIcon(this.editIcon)):e.push(this.genIcon(this.completeIcon)):e.push(String(this.step)),e},toggle:function(e){this.isActive=e.toString()===this.step.toString(),this.isInactive=Number(e)<Number(this.step)}},render:function(e){return e("div",{staticClass:"v-stepper__step",class:this.classes,directives:[{name:"ripple",value:this.editable}],on:{click:this.click}},[this.genStep(),this.genLabel()])}}),C=Object(u["a"])(p,a,o,!1,null,null,null),I=C.exports;h()(C,{VStepper:g,VStepperStep:$});var E=n("4360"),V=n("2f62"),w={name:"ExperimentSteps",data:function(){return{}},computed:{currentSection:function(){return this.$router.currentRoute.meta.currentSection},currentStep:function(){return this.$router.currentRoute.meta.currentStep},experiment:function(){return this.$store.state.experiment.experiment},routeExperimentId:function(){return this.$route.params.experiment_id}},beforeRouteEnter:function(e,t,n){if("ParticipationTypeConsentTitle"!==t.name||"ParticipationTypeConsentFile"!==e.name)return E["a"].dispatch("experiment/fetchExperimentById",e.params.experiment_id).then(n,n);n()},beforeRouteUpdate:function(e,t,n){if("ParticipationTypeConsentTitle"!==t.name||"ParticipationTypeConsentFile"!==e.name)return E["a"].dispatch("experiment/fetchExperimentById",e.params.experiment_id).then(n,n);n()},methods:Object(r["a"])({},Object(V["b"])({fetchExperimentById:"experiment/fetchExperimentById"})),components:{Steps:I}},B=w,j=(n("cd7c"),n("0798")),L=n("62ad"),O=n("a523"),P=n("132d"),T=n("0fd9"),A=Object(u["a"])(B,i,s,!1,null,"0e48f184",null);t["default"]=A.exports;h()(A,{VAlert:j["a"],VCol:L["a"],VContainer:O["a"],VIcon:P["a"],VRow:T["a"]})},8836:function(e,t,n){},abaf:function(e,t,n){},cd7c:function(e,t,n){"use strict";n("d692")},d692:function(e,t,n){},f8b9:function(e,t,n){"use strict";n("abaf")}}]);
//# sourceMappingURL=chunk-4257b527.e081345b.js.map