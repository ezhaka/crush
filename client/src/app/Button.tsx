import * as React from 'react';
import "./Button.css"
import {ReactNode} from "react";

type Props = {
    title: string;
    action: () => void;
}

export const Button = ({title, action}: Props) => {
    return (
        <div className="button button-label-hover-container" onClick={action}>
            <div className="button-label">
                <div className="button-label-label-text-2 button-label-transition">{title}</div>
                <div className="button-label-label-text-1 button-label-transition">{title}</div>
                <div className="button-label-label-text">{title}</div>
            </div>
        </div>
    )
}

type ButtonTitleProps = {
    children: ReactNode
}

export const ButtonTitle = ({children}: ButtonTitleProps) => (
    <div className="button-label">
        <div className="button-label-label-text-2 button-label-transition">{children}</div>
        <div className="button-label-label-text-1 button-label-transition">{children}</div>
        <div className="button-label-label-text">{children}</div>
    </div>
)