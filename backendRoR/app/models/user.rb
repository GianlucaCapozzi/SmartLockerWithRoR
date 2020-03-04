class User < ApplicationRecord
    has_secure_password
    '''around_update :notify_system_if_email_confirmed_is_changed

    private

    def notify_system_if_email_confirmed_is_changed
        email_confirmed = self.email_confirmed

        yield

        notify_system if email_confirmed
    end'''

end
