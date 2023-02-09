import * as React from "react";
import {ReactNode, useContext} from "react";
import {PageContext} from "./App";
import "./CloseableOverlay.css"
import "./RootPage.css"
import "../../resources/font/css/fontello.css"

type Props = {
    children: ReactNode
}

export const CloseableOverlay = ({children}: Props) => {
    const setPage = useContext(PageContext)

    return (<div className="closeable-overlay">
        <div className="noise" />
        <a
            href="#"
            className="icon-close"
            onClick={(e) => {
                e.preventDefault()
                setPage({kind: "root"})
            }}>
        </a>

        {children}
    </div>)
}