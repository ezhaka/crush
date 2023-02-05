import * as React from "react";
import {ReactNode, useContext} from "react";
import {PageContext} from "./App";
import "./CloseableOverlay.css"

type Props = {
    children: ReactNode
}

export const CloseableOverlay = ({children}: Props) => {
    const setPage = useContext(PageContext)

    return (<div className="closeable-overlay">
        <a
            href="#"
            className="close-link"
            onClick={(e) => {
                e.preventDefault()
                setPage({kind: "root"})
            }}>
        </a>

        {children}
    </div>)
}