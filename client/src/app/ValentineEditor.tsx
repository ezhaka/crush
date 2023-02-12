import * as React from "react";
import {ValentineType} from "./ValentineType";
import "./ValentineEditor.css"
import {useEffect, useLayoutEffect, useRef} from "react";

type Props = {
    type: ValentineType;
    message: string;
    setMessage: (message: string) => void;
}

export const ValentineEditor = ({type, message, setMessage}: Props) => {
    const src = require(`./../../resources/valentines/${type.name}-fullscreen.png`)
    const limit = 100
    const textareaRef = useRef<HTMLTextAreaElement>()

    // https://stackoverflow.com/a/24676492/2401247
    useLayoutEffect(() => {
        const element = textareaRef.current
        element.style.height = "auto";
        element.style.height = element.scrollHeight + "px";
    }, [textareaRef.current, message])

    return (
        <div className="valentine-editor">
            <img src={src}/>
            <div
                className="textarea-container"
                style={{
                    top: type.top + 'px',
                    left: type.left + 'px',
                    width: type.width + 'px',
                    height: type.height + 'px',
                }}
            >
                <textarea
                    ref={textareaRef}
                    placeholder="Add message here"
                    value={message}
                    onChange={(e) =>
                        setMessage(e.target.value.slice(0, limit).replace('\n', ''))
                    }
                    onKeyDown={(e) => {
                        // To prevent carousel from spinning
                        if (e.key === 'Right' || e.key === 'ArrowRight' || e.key === 'Left' || e.key === 'ArrowLeft') {
                            e.stopPropagation()
                        }
                    }}
                    style={{
                        color: type.textColor,
                        width: type.width + 'px',
                        maxHeight: type.height + 'px',
                    }}
                />
            </div>
        </div>
    )
}