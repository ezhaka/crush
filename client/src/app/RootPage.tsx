import * as React from 'react';
import {Button} from "./Button";
import {useContext, useMemo} from "react";
import {PageContext} from "./App";
import "./RootPage.css"
import {valentineTypes} from "./ValentineType";

type Props = {
    valentines: Valentine[]
}

const IncomingValentine = ({valentine}: { valentine: Valentine }) => {
    const valentineType = valentineTypes[valentine.type]
    const src = require(`./../../resources/valentines/${valentineType.name}-small.png`)
    const setPage = useContext(PageContext)

    return (<a
        href="#"
        className="incoming-valentine"
        onClick={(e) => {
            e.preventDefault()
            setPage({kind: "valentine", valentine})
        }}>
        <img src={src}/>
    </a>)
}

const IncomingValentineRow = ({valentines}: { valentines: Valentine[] }) => {
    return (<div className="incoming-valentines-row">
        {valentines.map(v => <IncomingValentine valentine={v}/>)}
    </div>)
}

export const RootPage = ({valentines}: Props) => {
    const setPage = useContext(PageContext)

    const rows = useMemo(() => spreadToRows(valentines), [valentines])

    const twoColumn = valentines && valentines.length > 0

    return (
        <div className={twoColumn ? "root-page_two-column" : "root-page"}>
            {twoColumn && <div className="left-column-background"></div>}
            <div className="noise"></div>

            <div className="root-page-content">
                <a href="#" onClick={() => {
                    const channel = new MessageChannel();
                    window.parent.postMessage({
                        type: "LeaveFullScreenRequest",
                    }, "*", [channel.port2]);
                }} className="back-to-space">Back To Space</a>

                <div className="logo"></div>

                <div className="root-page-columns">
                    {valentines && valentines.length > 0 && <div className="incoming-valentines-column">
                        <div className="promo-header">Someone crushed into you</div>

                        {rows.map(row => <IncomingValentineRow valentines={row}/>)}
                    </div>}

                    <div className="send-valentine-column">
                        <div className="promo-header">Make this Valentine's Day very special!</div>
                        <div className="promo-text">Our secure and confidential service ensures that your feelings will
                            reach your crush without revealing your identity. Choose from our selection of beautiful
                            valentines and add a secret message if you wish.
                        </div>

                        <Button title="Send secret valentine" action={() => {
                            setPage({kind: "sendForm"})
                        }}/>
                    </div>
                </div>
            </div>
        </div>

    )
}

function spreadToRows(valentines: Valentine[]) {
    const rows: Valentine[][] = []

    if (!valentines) {
        return rows
    }

    for (let i = 0; i < valentines.length; i++) {
        const valentine = valentines[i]

        if (!rows.length) {
            rows.push([valentine])
            continue
        }

        const lastRowIndex = rows.length - 1
        const lastRowTargetElementsCount = lastRowIndex % 2 == 0 ? 2 : 1;

        if (rows[lastRowIndex].length < lastRowTargetElementsCount) {
            rows[lastRowIndex].push(valentine)
        } else {
            rows.push([valentine])
        }
    }

    return rows
}
