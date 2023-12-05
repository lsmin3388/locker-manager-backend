package knu.cse.locker.manager.domain.locker.service;

import knu.cse.locker.manager.domain.account.entity.Account;
import knu.cse.locker.manager.domain.locker.dto.request.LockerChangeRequestDto;
import knu.cse.locker.manager.domain.locker.dto.request.LockerPasswordChangeRequestDto;
import knu.cse.locker.manager.domain.locker.entity.Locker;
import knu.cse.locker.manager.domain.locker.entity.LockerLocation;
import knu.cse.locker.manager.domain.locker.repository.LockerRepository;
import knu.cse.locker.manager.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerService {
    private final LockerRepository lockerRepository;

    @Transactional
    public Long changeLockerFromAccount(Account account, final LockerChangeRequestDto requestDto) {
        String lockerName = requestDto.getLockerName();

        Locker locker = findByLockerName(lockerName);

        unAssignAccountFromLocker(account); // 기존 연결 끊기
        locker.assignAccount(account); // 새로운 연결 하기

        return lockerRepository.save(locker).getId();
    }

    public Locker findByLockerName(String lockerName) {
        LockerLocation lockerLocation = getLockerLocationFromName(lockerName);
        String lockerNumber = getLockerNumberFromName(lockerName);

        return lockerRepository.findByLockerLocationAndLockerNumber(lockerLocation, lockerNumber)
                .orElseThrow(() -> new NotFoundException("Locker을 찾을 수 없습니다."));
    }

    private LockerLocation getLockerLocationFromName(String lockerName) {
        String loc_str = lockerName.split("-")[0];

        return switch (loc_str.toUpperCase()) {
            case "B1" -> LockerLocation.LOC_B1;
            case "L" -> LockerLocation.LOC_L;
            case "3F" -> LockerLocation.LOC_3F;
            default -> null;
        };
    }

    private String getLockerNumberFromName(String lockerName) {
        return lockerName.split("-")[1];
    }

    private void unAssignAccountFromLocker(Account account) {
        lockerRepository.findByAccount(account).ifPresent(locker_ -> {
            locker_.unAssignAccount();
            lockerRepository.save(locker_);
        });
    }

    public Long changeLockerPassword(Account account, LockerPasswordChangeRequestDto requestDto) {
        Locker locker = lockerRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException("사물함이 존재하지 않습니다."));

        locker.assignLockerPassword(requestDto.getLockerPassword());
        return lockerRepository.save(locker).getId();
    }
}
