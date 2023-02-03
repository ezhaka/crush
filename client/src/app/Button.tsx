import * as React from 'react';
import "./Button.css"

type Props = {
    title: string;
    action: () => void;
}

export const Button = ({title, action}: Props) => {
    return (
        <div className="button" onClick={action}>
            <div className="button-label">
                <div className="button-label-label-text-2 button-label-transition">{title}</div>
                <div className="button-label-label-text-1 button-label-transition">{title}</div>
                <div className="button-label-label-text">{title}</div>
            </div>
        </div>
    )
}