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
    initialValentine: Valentine;
    token: UserTokenData;
    markValentineAsRead: (valentine: Valentine) => void
}

export const ValentineViewPage = ({valentines, initialValentine, token, markValentineAsRead}: Props) => {
    const index = useMemo(() => {
        return valentines.findIndex(v => v.id == initialValentine.id)
    }, [initialValentine.id, valentines])

    const [activeSlide, setActiveSlide] = useState(index);

    const currentValentine = useMemo(() => valentines[activeSlide], [valentines, activeSlide])

    useEffect(() => {
        const fetch = async () => {
            if (token && !currentValentine.read) {
                await httpPut(`/api/read-valentine?valentineId=${currentValentine.id}`, token.token, {})
                markValentineAsRead(currentValentine)
            }
        }

        fetch().catch(console.error)
    }, [currentValentine.id, currentValentine.read, token, markValentineAsRead])

    return (
        <CloseableOverlay>
            <div className="valentine-view-page">

                {valentines.length === 1 && <div className="carousel-container hidden-scrollbar">
                    <ValentineView valentine={valentines[0]} token={token}/>
                </div> }

                {valentines.length > 1 && <ValentineCarousel
                    infinite={false}
                    slidesCount={valentines.length}
                    activeSlide={activeSlide}
                    setActiveSlide={setActiveSlide}
                >
                    {valentines.map(v => <ValentineView valentine={v} token={token}/>)}
                </ValentineCarousel>}
            </div>
        </CloseableOverlay>
    )
}

const ValentineView = ({valentine, token}: { valentine: Valentine, token: UserTokenData }) => {
    const type = valentineTypes[valentine.type]
    const src = require(`./../../resources/valentines/${type.name}-fullscreen.png`)

    return (
        <div className="valentine-view">
            <img src={src}/>
            <div className="valentine-message" style={{
                color: type.textColor,
                top: type.top + 'px',
                left: type.left + 'px',
                width: type.width + 'px',
                height: type.height + 'px',
            }} onMouseDown={(e) => e.stopPropagation()}>
                {valentine.message}
            </div>
        </div>
    )

}
