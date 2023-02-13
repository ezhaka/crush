import * as React from "react";
import {ReactNode, useContext, useEffect} from "react";
import {PageContext} from "./App";
import "./CloseableOverlay.css"
import "./RootPage.css"
import "../../resources/font/css/fontello.css"

type Props = {
    children: ReactNode
}

export const CloseableOverlay = ({children}: Props) => {
    const setPage = useContext(PageContext)

    useEffect(() => {
        const listener = (e: KeyboardEvent) => {
            if (e.key === 'Esc' || e.key === 'Escape') {
                setPage({kind: "root"})
            }
        }

        document.addEventListener('keydown', listener)

        return () => {
            document.removeEventListener('keydown', listener)
        }
    }, [])

    return (<div className="closeable-overlay">
        <div className="closeable-overlay-limited-container">
            <a
                href="#"
                className="icon-close"
                onClick={(e) => {
                    e.preventDefault()
                    setPage({kind: "root"})
                }}>
            </a>

            {children}
        </div>
    </div>)
}