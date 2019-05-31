package cn.rongcloud.sealclass.common;

import cn.rongcloud.sealclass.R;

public enum ErrorCode {
    API_ERR_REQUEST_PARA_ERR(1, R.string.error_api_common_error),
    API_ERR_INVALID_AUTH(2, R.string.error_api_invalid_auth),
    API_ERR_ACCESS_DENIED(3, R.string.error_api_permission_denied),
    API_ERR_BAD_REQUEST(4, R.string.error_api_common_error),
    API_ERR_OTHER(255, R.string.error_api_common_error),
    API_ERR_IM_TOKEN_ERROR(10, R.string.error_api_server_error),
    API_ERR_CREATE_ROOM_ERROR(11, R.string.error_api_create_room_error),
    API_ERR_JOIN_ROOM_ERROR(12, R.string.error_api_join_room_error),
    API_ERR_MESSAGE_ERROR(13, R.string.error_api_server_error),
    API_ERR_ROOM_NOT_EXIST(20, R.string.error_api_room_not_exist),
    API_ERR_USER_NOT_EXIST_IN_ROOM(21, R.string.error_api_user_not_in_room),
    API_ERR_EXIT_ROOM_ERROR(22, R.string.error_api_exit_room_error),
    API_ERR_LECTURER_NOT_EXIST_IN_ROOM(23, R.string.error_api_lecturer_not_in_room),
    API_ERR_ASSISTANT_NOT_EXIST_IN_ROOM(24, R.string.error_api_assistant_not_in_room),
    API_ERR_CREATE_WHITE_BOARD(25, R.string.error_api_create_white_board_error),
    API_ERR_WHITE_BOARD_NOT_EXIST(26, R.string.error_api_white_board_not_exist),
    API_ERR_DELETE_WHITE_BOARD(27, R.string.error_api_delete_white_error),
    API_ERR_USER_EXIST_IN_ROOM(28, R.string.error_api_already_in_room),
    API_ERR_CHANGE_SELF_ROLE(29, R.string.error_api_can_not_change_self_role),
    API_ERR_APPLY_TICKET_INVALID(30, R.string.error_api_ticket_invalid),
    API_ERR_OVER_MAX_COUNT(31, R.string.error_api_over_max_count),
    API_ERR_LECTURER_EXIST_IN_ROOM(32, R.string.error_api_lecturer_has_exist),
    API_ERR_DOWNGRADE_ROLE(33, R.string.error_api_downgrade_error),
    API_ERR_CHANGE_ROLE(34, R.string.error_api_change_role_error),
    NETWORK_ERROR(10000, R.string.error_network_error),
    IM_ERROR(10003, R.string.error_im_common_error),
    RTC_ERROR(10004, R.string.error_rtc_common_error),
    UNKNOWN_ERROR(99999, R.string.error_unkown_error),
    NONE_ERROR(-1, 0);

    private int code;
    private int messageResId;

    ErrorCode(int code, int messageResId) {
        this.code = code;
        this.messageResId = messageResId;
    }

    public int getCode() {
        return code;
    }

    public int getMessageResId() {
        return messageResId;
    }

    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code == code)
                return errorCode;
        }

        return UNKNOWN_ERROR;
    }

}
