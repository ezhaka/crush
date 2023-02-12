import {ReactFragment, ReactNode, useEffect, useMemo} from "react";
import Carousel from "react-simply-carousel";
import * as React from "react";
import "./Common.css"
import "./ValentineCarousel.css"

type Props = {
    slidesCount: number;
    activeSlide: number;
    setActiveSlide: (value: number) => void;
    infinite: boolean;
    children: ReactNode;
}

export const ValentineCarousel = ({slidesCount, activeSlide, setActiveSlide, infinite, children}: Props) => {
    const arrowStyles = {
        alignSelf: "center",
        background: "none",
        border: "none",
        padding: 0,
        width: 68
    };

    useEffect(() => {
        const listener = (e: KeyboardEvent) => {
            if (e.key === 'Right' || e.key === 'ArrowRight') {
                const nextSlide = activeSlide + 1;
                if (infinite) {
                    setActiveSlide(nextSlide % slidesCount)
                } else if (nextSlide < slidesCount) {
                    setActiveSlide(nextSlide)
                }
            }

            if (e.key === 'Left' || e.key === 'ArrowLeft') {
                const prevSlide = activeSlide - 1;
                if (infinite) {
                    setActiveSlide(prevSlide < 0 ? (slidesCount - 1) : prevSlide)
                } else if (prevSlide >= 0) {
                    setActiveSlide(prevSlide)
                }
            }
        };

        document.addEventListener('keydown', listener)

        return () => {
            document.removeEventListener('keydown', listener)
        }
    }, [slidesCount, activeSlide, setActiveSlide, infinite])

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
            infinite={infinite}
            disableNavIfEdgeVisible={true}
        >
            {children}
        </Carousel>
    </div>
}