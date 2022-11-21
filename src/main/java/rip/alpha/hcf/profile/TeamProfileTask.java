package rip.alpha.hcf.profile;

import lombok.RequiredArgsConstructor;
import rip.alpha.hcf.HCF;

@RequiredArgsConstructor
public class TeamProfileTask implements Runnable {

    private final TeamProfileHandler handler;

    @Override
    public void run() {
        try {
            int saved = 0;
            int removed = 0;
            for (TeamProfile profile : handler.getProfiles()) {
                if (profile.isSave()) {
                    this.handler.saveProfile(profile);
                    saved++;
                }

                if (profile.isRemove()) {

                    if (profile.toPlayer() != null){
                        continue;
                    }

                    this.handler.removeProfile(profile.getUuid());
                    profile.setRemove(false);
                    removed++;

                }
            }

            if (saved > 0) {
                int savedCount = saved;
                HCF.log(logger -> logger.info("Successfully saved " + savedCount + " profiles"));
            }

            if (removed > 0) {
                int removedCount = removed;
                HCF.log(logger -> logger.info("Successfully removed " + removedCount + " profiles"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
