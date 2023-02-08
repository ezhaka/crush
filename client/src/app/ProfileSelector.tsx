import {httpGet} from "../api/http";
import AsyncSelect from "react-select/async";
import * as React from "react";
import {UserTokenData} from "../UserTokenData";
import {ReactNode, useState} from "react";
import "./ProfileSelector.css"
import {ButtonTitle} from "./Button";
import {StylesConfig} from "react-select";
import "../../resources/font/css/fontello.css"

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
        color: 'white',
        border: 'none',
        borderWidth: 0,
        borderColor: 'transparent',
        boxShadow: 'none',
    }),
    input: (provided) => ({
        ...provided,
        color: 'white'
    }),
    menu: () => ({
        boxShadow: 'inset 0 1px 0 rgba(0, 0, 0, 0.1)'
    }),
    option: (provided, {isFocused, isSelected}) => ({
        ...provided,
        background: 'transparent',
        ...(isFocused ? {background: '#EB1863'} : {})
    }),
    menuList: (provided) => ({
        ...provided,
        paddingBottom: 0
    })
};

export const ProfileSelector = ({value, onChange, token}: Props) => {
    const [isOpen, setIsOpen] = useState(false);

    const loadOptions = async (
        inputValue: string,
    ) => {
        const responseRaw = await httpGet(`/api/get-profiles?query=${inputValue || ''}`, token.token)
        const response = await responseRaw.json() as ProfileListResponse
        return response.data.map(p => ({...p, value: p.id, label: `${p.firstName} ${p.lastName}`}))
    };

    return (
        <Dropdown
            isOpen={isOpen}
            onClose={() => setIsOpen(false)}
            target={
                <div
                    className="profile-selector-target button-label-hover-container"
                    onClick={() => setIsOpen((prev) => !prev)}
                >
                    <div>
                        <ButtonTitle>
                            <span>
                                {value ? value.label : 'CHOOSE YOUR CRUSH'}
                                <span className={isOpen ? "icon-arrow-up" : "icon-arrow-down"}/>
                            </span>
                        </ButtonTitle>
                    </div>
                </div>
            }
        >
            <AsyncSelect
                autoFocus
                backspaceRemovesValue={false}
                cacheOptions
                components={{DropdownIndicator: null, IndicatorSeparator: null}}
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
        {isOpen ? <Blanket onClick={onClose}/> : null}
    </div>
);