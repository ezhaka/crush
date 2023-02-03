import {httpGet} from "../api/http";
import AsyncSelect from "react-select/async";
import * as React from "react";
import {UserTokenData} from "../UserTokenData";
import {ReactNode, useState} from "react";
import "./ProfileSelector.css"
import {ButtonTitle} from "./Button";
import { StylesConfig } from "react-select";

export interface ProfileSelectItem extends Profile {
    value: string;
    label: string;
}

type Props = {
    value: ProfileSelectItem;
    onChange: (arg: ProfileSelectItem) => void;
    token: UserTokenData;
}

const selectStyles: StylesConfig<ProfileSelectItem, false> = {
    control: (provided) => ({
        ...provided,
        margin: 8,
        background: 'transparent',
        color: 'white'
    }),
    input: (provided) => ({
        ...provided,
        color: 'white'
    }),
    menu: () => ({
        boxShadow: 'inset 0 1px 0 rgba(0, 0, 0, 0.1)'
    }),
};

export const ProfileSelector = ({value, onChange, token}: Props) => {
    const [isOpen, setIsOpen] = useState(false);

    const loadOptions = async (
        inputValue: string,
    ) => {
        const responseRaw = await httpGet(`/homepage/get-profiles?query=${inputValue || ''}`, token.token)
        const response = await responseRaw.json() as ProfileListResponse
        return response.data.map(p => ({...p, value: p.id, label: `${p.firstName} ${p.lastName}`}))
    };

    return (
        <Dropdown
            isOpen={isOpen}
            onClose={() => setIsOpen(false)}
            target={
                <div className="profile-selector-target" onClick={() => setIsOpen((prev) => !prev)}>
                    <div>
                        <ButtonTitle title={value ? value.label : 'CHOOSE YOUR CRUSH'}/>
                    </div>
                    <ChevronDown/>
                </div>
            }
        >
            <AsyncSelect
                autoFocus
                backspaceRemovesValue={false}
                cacheOptions
                components={{ DropdownIndicator: null, IndicatorSeparator: null }}
                controlShouldRenderValue={false}
                defaultOptions
                hideSelectedOptions={false}
                isClearable={false}
                loadOptions={loadOptions}
                menuIsOpen
                onChange={(newValue) => {
                    onChange(newValue as ProfileSelectItem)
                    setIsOpen(false)
                }}
                // options={stateOptions}
                placeholder="Type to filter"
                styles={selectStyles}
                tabSelectsValue={false}
                value={value}
            />
        </Dropdown>
    )
}
// styled components

const Menu = (props: JSX.IntrinsicElements['div']) => {
    return (
        <div className="profile-selector-menu"
            {...props}
        />
    );
};
const Blanket = (props: JSX.IntrinsicElements['div']) => (
    <div className="profile-selector-blanket"
        {...props}
    />
);
const Dropdown = ({
                      children,
                      isOpen,
                      target,
                      onClose,
                  }: {
    children?: ReactNode;
    readonly isOpen: boolean;
    readonly target: ReactNode;
    readonly onClose: () => void;
}) => (
    <div className="profile-selector-dropdown">
        {target}
        {isOpen ? <Menu>{children}</Menu> : null}
        {isOpen ? <Blanket onClick={onClose} /> : null}
    </div>
);
const Svg = (p: JSX.IntrinsicElements['svg']) => (
    <svg
        width="24"
        height="24"
        viewBox="0 0 24 24"
        focusable="false"
        role="presentation"
        {...p}
    />
);
const DropdownIndicator = () => (
    <div className="profile-selector-dropdown-indicator">
        <Svg>
            <path
                d="M16.436 15.085l3.94 4.01a1 1 0 0 1-1.425 1.402l-3.938-4.006a7.5 7.5 0 1 1 1.423-1.406zM10.5 16a5.5 5.5 0 1 0 0-11 5.5 5.5 0 0 0 0 11z"
                fill="currentColor"
                fillRule="evenodd"
            />
        </Svg>
    </div>
);
const ChevronDown = () => (
    <Svg className="profile-selector-chevron-down">
        <path
            d="M8.292 10.293a1.009 1.009 0 0 0 0 1.419l2.939 2.965c.218.215.5.322.779.322s.556-.107.769-.322l2.93-2.955a1.01 1.01 0 0 0 0-1.419.987.987 0 0 0-1.406 0l-2.298 2.317-2.307-2.327a.99.99 0 0 0-1.406 0z"
            fill="currentColor"
            fillRule="evenodd"
        />
    </Svg>
);