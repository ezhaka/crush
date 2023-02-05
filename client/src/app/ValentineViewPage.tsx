import * as React from 'react';
import {CloseableOverlay} from "./CloseableOverlay";
import {valentineTypes} from "./ValentineType";
import "./ValentineViewPage.css";
import {useEffect} from "react";
import {httpPut} from "../api/http";
import {UserTokenData} from "../UserTokenData";

type Props = {
    valentine: Valentine;
    token: UserTokenData;
}

export const ValentineViewPage = ({valentine, token}: Props) => {
    const type = valentineTypes[valentine.type]
    const src = require(`./../../resources/valentines/${type.name}.png`)

    useEffect(() => {
        const fetch = async () => {
            if (token) {
                await httpPut(`/homepage/read-valentine?valentineId=${valentine.id}`, token.token, {})
            }
        }

        fetch().catch(console.error)
    }, [valentine.id, token])

    return (
        <CloseableOverlay>
            <div className="valentine-view-page">
                <div className="valentine-view">
                    <img src={src}/>
                    <div className="valentine-message" style={{
                        color: type.textColor,
                        top: type.top + 'px',
                        left: type.left + 'px'
                    }}>
                        {valentine.message}
                    </div>
                </div>
            </div>
        </CloseableOverlay>
    )
}

