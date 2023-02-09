import {ReactNode} from "react";
import Carousel from "react-simply-carousel";
import * as React from "react";
import "./Common.css"
import "./ValentineCarousel.css"

type Props = {
    activeSlide:number;
    setActiveSlide: (value: number) => void;
    children: ReactNode;
}

export const ValentineCarousel = ({children, activeSlide, setActiveSlide}: Props) => {
    const arrowStyles = {
        alignSelf: "center",
        background: "none",
        border: "none",
        padding: 0,
        cursor: "pointer",
        width: 68
    };

    return <div className="carousel-container hidden-scrollbar">
        <Carousel
            containerProps={{
                style: {
                    width: "1096px",
                    justifyContent: "space-between",
                    userSelect: "text",
                    flexWrap: "nowrap",
                }
            }}
            activeSlideIndex={activeSlide}
            activeSlideProps={{
                style: {
                    background: "blue",
                }
            }}
            onRequestChange={setActiveSlide}
            forwardBtnProps={{
                children: <div className="icon-arrow-right carousel-arrow"></div>,
                style: arrowStyles
            }}
            backwardBtnProps={{
                children: <div className="icon-arrow-left carousel-arrow"></div>,
                style: arrowStyles
            }}
            itemsToShow={1}
            speed={200}
        >
            {children}
        </Carousel>
    </div>
}