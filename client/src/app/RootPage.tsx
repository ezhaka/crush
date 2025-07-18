import * as React from 'react';
import {Button} from "./Button";
import {useContext, useEffect, useMemo} from "react";
import {PageContext} from "./App";
import "./Common.css"
import "./RootPage.css"
import {valentineTypes} from "./ValentineType";
import {UserTokenData} from "../UserTokenData";
import {httpGet} from "../api/http";

type Props = {
    valentines: Valentine[];
    setValentines: (valentines: Valentine[]) => void;
    token: UserTokenData;
}

const IncomingValentine = ({valentine}: { valentine: Valentine }) => {
    const valentineType = valentineTypes[valentine.type]
    const src = require(`./../../resources/valentines/${valentineType.name}-sticker.png`)
    const srcUnread = require(`./../../resources/valentines/${valentineType.name}-sticker-new.png`)
    const setPage = useContext(PageContext)

    return (<a
        href="#"
        className="incoming-valentine"
        onClick={(e) => {
            e.preventDefault()
            setPage({kind: "valentine", valentine})
        }}>
        {!valentine.read && <div className="new-heart"/>}
        <img src={valentine.read ? src : srcUnread}/>
    </a>)
}

const IncomingValentineRow = ({valentines}: { valentines: Valentine[] }) => {
    return (<div className="incoming-valentines-row">
        {valentines.map(v => <IncomingValentine key={v.id} valentine={v}/>)}
    </div>)
}

export const RootPage = ({valentines, setValentines, token}: Props) => {
    const setPage = useContext(PageContext)

    const rows = useMemo(() => spreadToRows(valentines), [valentines])

    const twoColumn = valentines && valentines.length > 0

    useEffect(() => {
        if (token?.token) {
            httpGet(`/api/get-valentines`, token.token)
                .then(res => {
                    if (res.ok) {
                        res.json().then(json => {
                            setValentines(json.data)
                        })
                    }
                })
        }
    }, [token?.token])

    return (
        <div className={twoColumn ? "root-page_two-column" : "root-page"}>
            {twoColumn && <div className="left-column-background"></div>}
            <div className="noise"></div>

            <div className="root-page-content">
                <div className="logo"></div>

                {valentines !== undefined && token && <div className="root-page-columns">
                    {valentines && valentines.length > 0 && <div className="incoming-valentines-column">
                        <div className="promo-header">Someone crushed into you</div>

                        {rows.map((row, index) => <IncomingValentineRow key={index} valentines={row}/>)}
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
                </div>}
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
