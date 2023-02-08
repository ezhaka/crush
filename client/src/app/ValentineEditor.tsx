import * as React from "react";
import {ValentineType} from "./ValentineType";
import "./ValentineEditor.css"

type Props = {
    type: ValentineType;
    message: string;
    setMessage: (message: string) => void;
}

export const ValentineEditor = ({type, message, setMessage}: Props) => {
    const src = require(`./../../resources/valentines/${type.name}.png`)

    const limit = 100

    return (
        <div className="valentine-editor">
            <img src={src}/>
            <textarea
                placeholder="Add message here"
                value={message}
                onChange={(e) => setMessage(e.target.value.slice(0, limit))}
                style={{
                    color: type.textColor,
                    top: type.top + 'px',
                    left: type.left + 'px'
                }}
            />
        </div>
    )
}