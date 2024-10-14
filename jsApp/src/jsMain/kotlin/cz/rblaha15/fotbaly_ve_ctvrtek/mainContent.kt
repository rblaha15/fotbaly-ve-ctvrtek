package cz.rblaha15.fotbaly_ve_ctvrtek

import io.kvision.core.BsColor
import io.kvision.core.Container
import io.kvision.core.addBsColor
import io.kvision.core.onChange
import io.kvision.core.onClick
import io.kvision.form.select.select
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.icon
import io.kvision.html.image
import io.kvision.html.main
import io.kvision.html.nav
import io.kvision.html.p
import io.kvision.state.bind
import io.kvision.utils.px
import io.kvision.utils.rem

fun Container.mainContent(
    viewModel: ManViewModel,
) {
    navBar()
    main {
        addCssClass("container")
        addCssClass("my-3")

        nameSelect(viewModel)
        myAnswerSelector(viewModel)
        counter(viewModel)
        answerList(viewModel)
    }
}

fun Container.counter(viewModel: ManViewModel) {
    p().bind(viewModel.count) {
        content = "Kolik nás bude: $it"
    }
}

private fun Container.navBar() = nav {
    addCssClass("navbar")
    addCssClass("bg-body-tertiary")
    div {
        addCssClass("container-fluid")
        div {
            addCssClass("navbar-brand")
            addCssClass("me-auto")
            image(
                src = "./icon.jpg"
            ) {
                addCssClass("d-inline-block")
                addCssClass("align-text-top")
                addCssClass("me-2")
                width = 32.px
                height = 32.px
            }
            +"Čutání u Jelena"
        }

//        val userAgent = window.navigator.userAgent.lowercase()
//        if ("android" in userAgent) {
//            link(
//                "Stáhnout aplikaci",
//                icon = "bi-mobile",
//                url = "https://github.com/rblaha15/fotbaly-ve-ctvrtek/releases/latest/download/fotbaly-ve-ctvrtek.apk"
//            ) {
//            onClick {
//                (document.createElement("iframe") as HTMLIFrameElement).apply {
//                    val url = "intent://fotbaly-ve-ctvrtek.web.app#Intent;scheme=https;package=cz.rblaha15.fotbaly_ve_ctvrtek;end"
//                    src = url
//                    onload = {
//                        window.open("https://github.com/rblaha15/fotbaly-ve-ctvrtek/release/download/latest/fotbaly-ve-ctvrtek.apk")
//                    }
//                    style.display = "none"
//                    document.body?.appendChild(this)
//
//                    window.setTimeout({
//                        document.body?.removeChild(this)
//                    }, 1000)
//                }
//            }
//            }
//        }
    }
}

private fun Container.answerList(viewModel: ManViewModel) = div()
    .bind(viewModel.answers) { answers ->
        addCssClass("mt-3")
        answers.forEach { (name, answer) ->
            div {
                addCssClass("d-flex")
                addCssClass("flex-row")
                addCssClass("align-items-center")
                p {
                    addCssClass("flex-grow-1")
                    +name
                }
                answers(answer)
            }
        }
    }

private fun Container.myAnswerSelector(viewModel: ManViewModel) = div()
    .bind(viewModel.myAnswer) { myAnswer ->
        addCssClass("d-flex")
        addCssClass("flex-row")
        addCssClass("align-items-center")
        addCssClass("mt-3")
        p {
            addCssClass("flex-grow-1")
            +"Půjdeš ve čtvrtek na fotbal?"
        }
        answers(
            selected = myAnswer,
            onClick = { state ->
                viewModel.setMyAnswer(state)
            }
        )
    }

//private fun Container.nameSelect(viewModel: ManViewModel) = text(
//    label = "Jméno",
//    floating = true,
//) {
//    onInput {
//        viewModel.setName(value ?: "")
//    }
//}.bind(viewModel.name) {
//    value = it
//}
private fun Container.nameSelect(viewModel: ManViewModel) = select(
    label = "Jméno",
    floating = true,
) {
    onChange {
        viewModel.setName(value ?: "")
    }
    placeholder = "Vyber své jméno"
}.bind(viewModel.name) {
    value = it
}.bind(viewModel.people) {
    options = it.pairs()
}

private fun List<String>.pairs() = map { it to it }

private fun Div.answers(
    selected: AnswerState?,
    onClick: ((AnswerState) -> Unit)? = null,
) = AnswerState.entries.forEach { state ->
    icon(
        when (state) {
            AnswerState.Yes -> "bi-check"
            AnswerState.No -> "bi-x"
            AnswerState.Maybe -> "bi-question"
        }
    ) {
        addCssClass("ms-2")
        addCssClass("btn")
        if (onClick == null) addCssClass("disabled")
        setStyle("border", "none")
        setStyle("opacity", "1")
        onClick?.let { this.onClick { onClick(state) } }
        fontSize = 1.5.rem
        addBsColor(
            when {
                state == selected -> when (state) {
                    AnswerState.Yes -> BsColor.SUCCESS
                    AnswerState.No -> BsColor.DANGER
                    AnswerState.Maybe -> BsColor.WARNING
                }

                else -> BsColor.BODY
            }
        )
    }
}