import * as React from 'react';
import {CloseableOverlay} from "./CloseableOverlay";
import {valentineTypes} from "./ValentineType";
import "./ValentineViewPage.css";
import "./Common.css";
import {useEffect, useMemo, useState} from "react";
import {httpPut} from "../api/http";
import {UserTokenData} from "../UserTokenData";
import {ValentineCarousel} from "./ValentineCarousel";

type Props = {
    valentines: Valentine[];
    valentine: Valentine;
    token: UserTokenData;
}

export const ValentineViewPage = ({valentines, valentine, token}: Props) => {
    const index = useMemo(() => {
        return valentines.findIndex(v => v.id == valentine.id)
    }, [valentine.id, valentines])

    const [activeSlide, setActiveSlide] = useState(index);

    return (
        <CloseableOverlay>
            <div className="valentine-view-page">
                <ValentineCarousel activeSlide={activeSlide} setActiveSlide={setActiveSlide}>
                    {valentines.map(v => <ValentineView valentine={v} token={token}/>)}
                </ValentineCarousel>
            </div>
        </CloseableOverlay>
    )
}

const ValentineView = ({valentine, token}: { valentine: Valentine, token: UserTokenData }) => {
    const type = valentineTypes[valentine.type]
    const src = require(`./../../resources/valentines/${type.name}-fullscreen.png`)

    useEffect(() => {
        const fetch = async () => {
            if (token) {
                await httpPut(`/api/read-valentine?valentineId=${valentine.id}`, token.token, {})
            }
        }

        fetch().catch(console.error)
    }, [valentine.id, token])

    return (
        <div className="valentine-view">
            <img src={src}/>
            <div className="valentine-message" style={{
                color: type.textColor,
                top: type.top + 'px',
                left: type.left + 'px',
                width: type.width + 'px',
                height: type.height + 'px',
            }}>
                {valentine.message}
            </div>
        </div>
    )

}
