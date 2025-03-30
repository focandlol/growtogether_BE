package focandlol.domain.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotiType {

    BOOTCAMP(true),
    STUDY(true),
    VOTE(true);

    private final boolean importType;

    public boolean importNoti(){
        return importType;
    }
}
